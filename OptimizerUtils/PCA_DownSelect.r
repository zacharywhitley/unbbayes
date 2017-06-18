#------------------------------------------------
# MAIN function for computing detector alert days
# Input:
# - datafile = csv file with training and test data in long format
#   Alternatively can specify input arrays: X, Y, U2G
# - X   = multidimensional array containing training data
# - Y   = multidimensional array containing test data
# - U2G = data frame mapping users to peer groups
# - CompGrp = bool to indicate whether to compute group detector alert days
# Output: List with components (user, group, P99, P99g, U2G)
# - user  = user detector alert days
# - group = peer group detector alert days
# - p99   = user distance 99 percentiles
# - p99g  = peer group distance 99 percentiles
# - U2G   = user to group mapping
# Note: 
# 1. specify input using (a) datafile or (b) X,Y and U2G
# 2. if CompGrp=F group and P99g are set to NULL
#------------------------------------------------
ComputeAlertDays <- function(datafile, X=NULL, Y=NULL, U2G=NULL, CompGrp=T){
  if(!missing(datafile)){
    dataArray <- ReadRawScores(datafile)
    X <- dataArray$X
    Y <- dataArray$Y
    U2G <- dataArray$U2G
  }
  else {
    if(is.null(X) || is.null(Y) || is.null(U2G))
      stop("Must specify arrays 'X', 'Y' and 'U2G' if 'datafile' is not specified.")
  }
  
  G <- U2G$grpid

  nTrn <- dim(X)[1]
  nTim <- dim(X)[2]
  nUse <- dim(X)[3]
  nAct <- dim(X)[4]
  nTst <- dim(Y)[1]
  
  #------------------------------------------------
  # Part 1: (1.4.3) Individual user history detectors (detectors 1-12)
  #------------------------------------------------
  
  # Step 1: Compute mX and sX = user mean and SD
  # - mX and sdX are arrays of dimension c(nTim, nUse,nAct)
  mX <- apply(X, c(3,4), function(Xij){apply(Xij, 2, mean)} )
  sX <- apply(X, c(3,4), function(Xij){apply(Xij, 2, sd)} )
  # - step 1 used denominator n
  sX <- sX*sqrt((nTrn-1)/nTrn)
  
  # Steps 2 & 6: Compute ZX(nTrn, nTim, nUse,nAct) and ZY(nTst, nTim, nUse,nAct)
  # - arrays of standardized X and Y wrt mX and sX
  ZX <- X*0
  ZY <- Y*0
  for(i in 1:nUse){
    for(j in 1:nAct){
      for(k in 1:nTrn){
        ZX[k,,i,j] <- (X[k,,i,j] - mX[,i,j])/sX[,i,j]
	}
      for(k in 1:nTst){
        ZY[k,,i,j] <- (Y[k,,i,j] - mX[,i,j])/sX[,i,j]
	}
    }
  }

  # workaroud: if NaN happened, sX[,i,j was zero, but (X[k,,i,j] - mX[,i,j]) or (Y[k,,i,j] - mX[,i,j])
  # was also zero, so consider the division 0/0 as zero instead of NaN.
  ZX[is.na(ZX)] = 0
  
  # Steps 3,4 & 7: Compute DZX(nTrn, nUse, nAct) and DZY(nTst, nUse, nAct)
  # - distance arrays of ZX[,,i,j] and ZY[,,i,j] time-block vectors wrt PCA space of ZX
  DZX <- array(0, c(nTrn, nUse, nAct))
  DZY <- array(0, c(nTst, nUse, nAct))
  for(i in 1:nUse){
    for(j in 1:nAct){
      s <- svd(ZX[,,i,j])
      s$d[s$d<1e-4] <- 1e-4
      DZX[,i,j] <- apply( (ZX[,,i,j] %*% s$v / outer(rep(1,nTrn), s$d))^2 , 1, function(xx) {sqrt(sum(xx))})
      DZY[,i,j] <- apply( (ZY[,,i,j] %*% s$v / outer(rep(1,nTst), s$d))^2 , 1, function(xx) {sqrt(sum(xx))})
    }
  }
  
  # Step 5: Compute P99 = 99th percentile of distances
  P99 <- apply(DZX, c(2,3), quantile, probs=.99)
  
  
  # Step 8: Compute number of distances DZY[,i,j]>P99[i,j]
  A <- array(0, c(nUse, nAct))
  for(i in 1:nUse){
    A[i,] <- apply( DZY[,i,] > outer(rep(1,nTst), P99[i,]), 2, sum)
  }
  A <- as.data.frame(A)
  names(A) <- paste("ADD", 1:nAct, sep="")
  
  if(!CompGrp)
    return( list(user=A, group=NULL, P99=P99, P99g=NULL, U2G=U2G) )
  

  #------------------------------------------------
  # Part 2: (1.4.4) Peer groupd history detectors (detectors 13-24)
  #------------------------------------------------
  
  # Step 1: Compute mXg and sXg = peer group mean and SD for user
  # - mX and sdX are arrays of dimension c(nTim, nUse,nAct)
  
  # var(x) = ex2 - (ex)^2 : compute ex2 for each user
  eX2 <- array(0, c(nTim, nUse,nAct))
  for(i in 1:nUse){
    for(j in 1:nAct){
      eX2[,i,j] <- sX[,i,j]^2 + mX[,i,j]^2
    }
  }
  
  mXg <- array(0, c(nTim, nUse,nAct))
  sXg <- array(0, c(nTim, nUse,nAct))
  for(j in 1:nAct){
    for(g in unique(G)){
      gIdx <- (1:nUse)[G==g]
      gSize <- length(gIdx)
      # sum of mX and eX2 in group=g
      sum.mX <- apply(mX[,gIdx,j], 1, sum)
      sum.eX2 <- apply(eX2[,gIdx,j], 1, sum)
      for(i in gIdx){
        mXg[,i,j] <- (sum.mX - mX[,i,j])/(gSize-1)
        sXg[,i,j] <- sqrt((sum.eX2 - eX2[,i,j])/(gSize-1) - mXg[,i,j]^2)
      }
    }
  }
  
  
  # Steps 2 & 6: Compute ZXg(nTrn, nTim, nUse,nAct) and ZYg(nTst, nTim, nUse,nAct)
  # - arrays of standardized X and Y wrt mXg and sXg
  ZXg <- X*0
  ZYg <- Y*0
  for(i in 1:nUse){
    for(j in 1:nAct){
      for(k in 1:nTrn)
        ZXg[k,,i,j] <- (X[k,,i,j] - mXg[,i,j])/sXg[,i,j]
      for(k in 1:nTst)
        ZYg[k,,i,j] <- (Y[k,,i,j] - mXg[,i,j])/sXg[,i,j]
    }
  }
  
  # Steps 3,4 & 7: Compute DZXg(nTrn, nUse, nAct) and DZYg(nTst, nUse, nAct)
  # - distance arrays of ZXg[,,i,j] and ZYg[,,i,j] time-block vectors wrt peer group PCA space of ZX
  DZXg <- array(0, c(nTrn, nUse, nAct))
  DZYg <- array(0, c(nTst, nUse, nAct))
  for(j in 1:nAct){
    for(g in unique(G)){
      gIdx <- (1:nUse)[G==g]
      gSize <- length(gIdx)
      for(i in gIdx){
        gIdx.i <- setdiff(gIdx,i)
        ZXg.i <- ZXg[,, gIdx.i, j]
        ZXg.i <- aperm(ZXg.i, c(1,3,2))
        dim(ZXg.i) <- c(nTrn*(gSize-1), nTim)
        s <- svd(ZXg.i)
        s$d[s$d<1e-4] <- 1e-4
        DZXg[,i,j] <- apply( (ZXg[,,i,j] %*% s$v / outer(rep(1,nTrn), s$d))^2 , 1, function(xx) {sqrt(sum(xx))})
        DZYg[,i,j] <- apply( (ZYg[,,i,j] %*% s$v / outer(rep(1,nTst), s$d))^2 , 1, function(xx) {sqrt(sum(xx))})
      }
    }
  }
  
  
  # Step 5: Compute P99g = 99th percentile of peer group distances
  P99g <- apply(DZXg, c(2,3), quantile, probs=.99)
  
  
  # Step 8: Compute number of distances DZYg[,i,j]>P99[i,j]
  Ag <- array(0, c(nUse, nAct))
  for(i in 1:nUse){
    Ag[i,] <- apply( DZYg[,i,] > outer(rep(1,nTst), P99g[i,]), 2, sum)
  }
  Ag <- as.data.frame(Ag)
  names(Ag) <- paste("ADD", nAct+(1:nAct), sep="")


  list(user=A, group=Ag, P99=P99, P99g=P99g, U2G=U2G)  
}


#------------------------------------------------
# Helper function for creating raw score data file in long format from multidim array format
# Output: CSV data file in long format
#------------------------------------------------
CreateRawScoreFile <- function(X, Y, U2G, outfile){
  nTrn <- dim(X)[1]
  nTim <- dim(X)[2]
  nUse <- dim(X)[3]
  nAct <- dim(X)[4]
  nTst <- dim(Y)[1]
  
  X.long <- aperm(X, c(1,3,2,4) )
  dim(X.long) <- c(nTrn*nTim*nUse , nAct)
  colnames(X.long) <- paste("det", 1:12, sep="")
  X.long <- cbind(timeid = rep(1:nTim, rep(nTrn*nUse, nTim)),
                  userid = rep(rep(1:nUse, rep(nTrn, nUse)), nTim),
                  dayid  = rep(1:nTrn, nTim*nUse),
                  X.long
  )
  X.long <- as.data.frame(X.long)
  
  Y.long <- aperm(Y, c(1,3,2,4) )
  dim(Y.long) <- c(nTst*nTim*nUse , nAct)
  colnames(Y.long) <- paste("det", 1:12, sep="")
  Y.long <- cbind(timeid = rep(1:nTim, rep(nTst*nUse, nTim)),
                  userid = rep(rep(1:nUse, rep(nTst, nUse)), nTim),
                  dayid  = rep(1:nTst, nTim*nUse),
                  Y.long
  )
  Y.long <- as.data.frame(Y.long)
  
  XY.long <- rbind(cbind(type=1,X.long),
                   cbind(type=2,Y.long))
  XY.long <- merge(U2G, XY.long, by="userid", sort=F)
  attach(XY.long)
  XY.long <- XY.long[order(type, timeid, grpid, userid, dayid), ]
  detach(XY.long)
  write.csv(XY.long[,c(3,4,2,1,5:17)], file=outfile, row.names=F)
}


#------------------------------------------------
# Helper function for reading raw score data file in long format to multidim array format
# Output: List with components (X, Y, U2G). 
# - X and Y are multidimensional array representation of training and test data.
# - U2G is data frame of user to group mapping
#------------------------------------------------
ReadRawScores <- function(infile){
  XYlong <- read.csv(file=infile)
  
  # user to group map
  ug <- unique(XYlong[,c("grpid","userid")])
  ug <- ug[order(ug$userid), ]
  
  #nUse <- length(unique(Xlong$userid))
  nUse <- length(unique(ug$userid))
  # columns of detector scores
  detColIdx <- grep("det",tolower(names(XYlong)))
  nAct <- length(detColIdx)
  # number of time blocks
  nTim <- length(unique(XYlong$timeid))
  
  # training data
  Xlong <- subset(XYlong, type==1)
  attach(Xlong)
  Xlong <- Xlong[order(timeid, grpid, userid, dayid), ]
  detach(Xlong)
  nTrn <- length(unique(Xlong$dayid))
  Xarray <- as.matrix(Xlong[,detColIdx])
  dim(Xarray) <- c(nTrn, nUse, nTim, nAct)
  Xarray <- aperm(Xarray, c(1, 3, 2, 4) )
  
  # test data
  Ylong <- subset(XYlong, type==2)
  attach(Ylong)
  Ylong <- Ylong[order(timeid, grpid, userid, dayid), ]
  detach(Ylong)
  nTst <- length(unique(Ylong$dayid))
  Yarray <- as.matrix(Ylong[,detColIdx])
  dim(Yarray) <- c(nTst, nUse, nTim, nAct)
  Yarray <- aperm(Yarray, c(1, 3, 2, 4) )
  
  # return train and test data and group IDs of users
  list(X=Xarray, Y=Yarray, U2G=ug)
}

