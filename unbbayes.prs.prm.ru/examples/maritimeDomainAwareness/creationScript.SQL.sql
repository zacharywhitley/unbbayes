-- MDA STUDY CASE
-- DB CREATION SCRIPT.
-- Generation Time: Feb 12, 2013 at 01:42 PM
-- Server version: 5.5.28
-- PHP Version: 5.4.6-1ubuntu1.1

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
-- Table structure for table `BEHAVIOR`
--

CREATE TABLE IF NOT EXISTS `BEHAVIOR` (
  `id` int(11) NOT NULL,
  `name` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `BEHAVIOR`
--

INSERT INTO `BEHAVIOR` (`id`, `name`) VALUES
(1, 'EvasiveBehavior'),
(2, 'NormalBehavior');

-- --------------------------------------------------------

--
-- Table structure for table `ELECTRONIC_EQUIPMENT`
--

CREATE TABLE IF NOT EXISTS `ELECTRONIC_EQUIPMENT` (
  `id` int(11) NOT NULL,
  `isWorking` varchar(1) DEFAULT NULL,
  `isResponsive` varchar(1) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `owner` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `owner` (`owner`),
  KEY `type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ELECTRONIC_EQUIPMENT`
--

INSERT INTO `ELECTRONIC_EQUIPMENT` (`id`, `isWorking`, `isResponsive`, `type`, `owner`) VALUES
(1, 'T', 'T', 1, NULL),
(2, 'F', 'F', 2, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `EQUIPMENT_TYPE`
--

CREATE TABLE IF NOT EXISTS `EQUIPMENT_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(10) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;

--
-- Dumping data for table `EQUIPMENT_TYPE`
--

INSERT INTO `EQUIPMENT_TYPE` (`id`, `name`) VALUES
(1, 'Nuclear Bo'),
(2, 'Ant killer');

-- --------------------------------------------------------

--
-- Table structure for table `MEETING`
--

CREATE TABLE IF NOT EXISTS `MEETING` (
  `ship1` int(11) DEFAULT NULL,
  `ship2` int(11) DEFAULT NULL,
  KEY `ship1` (`ship1`),
  KEY `ship2` (`ship2`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `MEETING`
--

INSERT INTO `MEETING` (`ship1`, `ship2`) VALUES
(1, 2);

-- --------------------------------------------------------

--
-- Table structure for table `ORGANIZATION`
--

CREATE TABLE IF NOT EXISTS `ORGANIZATION` (
  `id` int(30) NOT NULL,
  `name` varchar(30) NOT NULL,
  `isTerrorist` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ORGANIZATION`
--

INSERT INTO `ORGANIZATION` (`id`, `name`, `isTerrorist`) VALUES
(1, 'The Company', 'T'),
(2, 'The Dogs', 'N'),
(3, 'The NN', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `PERSON`
--

CREATE TABLE IF NOT EXISTS `PERSON` (
  `id` varchar(30) NOT NULL,
  `relatedTo` varchar(30) DEFAULT NULL,
  `isTerrorist` varchar(1) DEFAULT NULL,
  `organization` int(11) DEFAULT NULL,
  `crewMemberOf` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `relatedTo` (`relatedTo`),
  KEY `organization` (`organization`),
  KEY `crewMember` (`crewMemberOf`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `PERSON`
--

INSERT INTO `PERSON` (`id`, `relatedTo`, `isTerrorist`, `organization`, `crewMemberOf`) VALUES
('Burrows', 'Scolfield', NULL, 1, 2),
('pato', NULL, 'T', NULL, 4),
('pato2', 'pato', NULL, NULL, 4),
('Scolfield', NULL, 'F', 3, 4),
('snoopy', NULL, NULL, NULL, NULL),
('Terrorist', NULL, 'T', NULL, NULL),
('TerroristRelated', 'Terrorist', 'F', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `ROUTE`
--

CREATE TABLE IF NOT EXISTS `ROUTE` (
  `id` int(11) NOT NULL,
  `name` varchar(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `ROUTE`
--

INSERT INTO `ROUTE` (`id`, `name`) VALUES
(0, 'UnusualRoute'),
(1, 'UsualRoute');

-- --------------------------------------------------------

--
-- Table structure for table `SHIP`
--

CREATE TABLE IF NOT EXISTS `SHIP` (
  `id` int(11) NOT NULL,
  `isOfInterest` varchar(1) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `isECMDeployed` varchar(1) DEFAULT NULL,
  `hasTerroristCrew` varchar(1) DEFAULT NULL,
  `behavior` int(11) DEFAULT NULL,
  `route` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `type` (`type`),
  KEY `behavior` (`behavior`),
  KEY `route` (`route`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `SHIP`
--

INSERT INTO `SHIP` (`id`, `isOfInterest`, `type`, `isECMDeployed`, `hasTerroristCrew`, `behavior`, `route`) VALUES
(1, 'N', NULL, 'N', 'N', NULL, 0),
(2, 'T', NULL, 'T', 'T', NULL, NULL),
(4, NULL, NULL, NULL, NULL, NULL, 1),
(5, NULL, NULL, NULL, NULL, NULL, 1);

-- --------------------------------------------------------

--
-- Table structure for table `SHIP_TYPE`
--

CREATE TABLE IF NOT EXISTS `SHIP_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(10) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=4 ;

--
-- Dumping data for table `SHIP_TYPE`
--

INSERT INTO `SHIP_TYPE` (`id`, `name`) VALUES
(1, 'NavyShip'),
(2, 'FishingShi'),
(3, 'MerchantSh');

--
-- Constraints for dumped tables
--

--
-- Constraints for table `ELECTRONIC_EQUIPMENT`
--
ALTER TABLE `ELECTRONIC_EQUIPMENT`
  ADD CONSTRAINT `ELECTRONIC_EQUIPMENT_ibfk_1` FOREIGN KEY (`owner`) REFERENCES `SHIP` (`id`),
  ADD CONSTRAINT `ELECTRONIC_EQUIPMENT_ibfk_2` FOREIGN KEY (`type`) REFERENCES `EQUIPMENT_TYPE` (`id`);

--
-- Constraints for table `MEETING`
--
ALTER TABLE `MEETING`
  ADD CONSTRAINT `MEETING_ibfk_1` FOREIGN KEY (`ship1`) REFERENCES `SHIP` (`id`),
  ADD CONSTRAINT `MEETING_ibfk_10` FOREIGN KEY (`ship2`) REFERENCES `SHIP` (`id`);

--
-- Constraints for table `PERSON`
--
ALTER TABLE `PERSON`
  ADD CONSTRAINT `PERSON_ibfk_10` FOREIGN KEY (`relatedTo`) REFERENCES `PERSON` (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
  ADD CONSTRAINT `PERSON_ibfk_8` FOREIGN KEY (`organization`) REFERENCES `ORGANIZATION` (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
  ADD CONSTRAINT `PERSON_ibfk_9` FOREIGN KEY (`crewMemberOf`) REFERENCES `SHIP` (`id`) ON DELETE SET NULL ON UPDATE SET NULL;

--
-- Constraints for table `SHIP`
--
ALTER TABLE `SHIP`
  ADD CONSTRAINT `SHIP_ibfk_2` FOREIGN KEY (`type`) REFERENCES `SHIP_TYPE` (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
  ADD CONSTRAINT `SHIP_ibfk_3` FOREIGN KEY (`behavior`) REFERENCES `BEHAVIOR` (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
  ADD CONSTRAINT `SHIP_ibfk_5` FOREIGN KEY (`route`) REFERENCES `ROUTE` (`id`) ON DELETE SET NULL ON UPDATE SET NULL;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;