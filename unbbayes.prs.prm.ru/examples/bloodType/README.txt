
Derby DB
jdbc:derby:examples/bloodType/BloodType.db
jdbc:derby:/home/dav/workspace-unb/unbbayes.prs.prm2/examples/bloodType/BloodType.db 


MySQL
jdbc:mysql://localhost:3306/BloodType?user=root&password=fds


--
-- Table structure for table `PERSON`
--
CREATE TABLE IF NOT EXISTS `PERSON` (
  `id` VARCHAR(11) NOT NULL,
  `Father` VARCHAR(11) DEFAULT NULL,
  `Mother` VARCHAR(11) DEFAULT NULL,
  `BloodType` VARCHAR(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `Father` (`Father`),
  KEY `Mother` (`Mother`)
) ;
--
-- Constraints for table `PERSON`
--
ALTER TABLE `PERSON`
  ADD CONSTRAINT `PERSON_ibfk_2` FOREIGN KEY (`Mother`) REFERENCES `PERSON` (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
  ADD CONSTRAINT `PERSON_ibfk_1` FOREIGN KEY (`Father`) REFERENCES `PERSON` (`id`) ON DELETE SET NULL ON UPDATE SET NULL;