-- Database schema -----
	
ALTER TABLE `ELECTRONIC_EQUIPEMENT` ADD FOREIGN KEY (`owner`) REFERENCES `MDA`.`SHIP`(`id`) ;


ALTER TABLE `PERSON` ADD FOREIGN KEY (`relatedTo`) REFERENCES `MDA`.`PERSON`(`id`) ;
ALTER TABLE `PERSON` ADD FOREIGN KEY (`organization`) REFERENCES `MDA`.`ORGANIZATION`(`id`) ;
ALTER TABLE `PERSON` ADD FOREIGN KEY (`crewMember`) REFERENCES `MDA`.`SHIP`(`id`) ;

ALTER TABLE `MEETING` ADD FOREIGN KEY (`ship1`) REFERENCES `MDA`.`SHIP`(`id`) ;
ALTER TABLE `MEETING` ADD FOREIGN KEY (`ship2`) REFERENCES `MDA`.`SHIP`(`id`) ;
	


	
-- phpMyAdmin SQL Dump
-- version 3.4.10.1deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Oct 05, 2012 at 11:19 AM
-- Server version: 5.5.24
-- PHP Version: 5.3.10-1ubuntu3.4

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `MDA`
--

-- --------------------------------------------------------

--
-- Table structure for table `ELECTRONIC_EQUIPEMENT`
--

CREATE TABLE IF NOT EXISTS `ELECTRONIC_EQUIPEMENT` (
  `id` int(11) NOT NULL,
  `isWorking` varchar(1) DEFAULT NULL,
  `isResponsive` varchar(1) DEFAULT NULL,
  `type` varchar(30) DEFAULT NULL,
  `owner` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `MEETING`
--

CREATE TABLE IF NOT EXISTS `MEETING` (
  `ship1` int(11) DEFAULT NULL,
  `ship2` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `ORGANIZATION`
--

CREATE TABLE IF NOT EXISTS `ORGANIZATION` (
  `id` varchar(30) NOT NULL,
  `terrorist` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `PERSON`
--

CREATE TABLE IF NOT EXISTS `PERSON` (
  `id` varchar(30) NOT NULL,
  `relatedTo` varchar(30) DEFAULT NULL,
  `terrorist` varchar(1) DEFAULT NULL,
  `organization` varchar(30) DEFAULT NULL,
  `crewMember` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `SHIP`
--

CREATE TABLE IF NOT EXISTS `SHIP` (
  `id` int(11) NOT NULL,
  `interested` varchar(1) DEFAULT NULL,
  `typeOfShip` varchar(30) DEFAULT NULL,
  `ecmDeployed` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;