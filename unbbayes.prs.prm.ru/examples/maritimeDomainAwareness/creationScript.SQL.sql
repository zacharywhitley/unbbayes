-- MDA STUDY CASE
-- DB CREATION SCRIPT.
-- Generation Time: Oct 06, 2012 at 05:40 PM

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

-- --------------------------------------------------------

--
-- Table structure for table `EQUIPMENT_TYPE`
--

CREATE TABLE IF NOT EXISTS `EQUIPMENT_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(10) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

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

-- --------------------------------------------------------

--
-- Table structure for table `PERSON`
--

CREATE TABLE IF NOT EXISTS `PERSON` (
  `id` varchar(30) NOT NULL,
  `relatedTo` varchar(30) DEFAULT NULL,
  `isTerrorist` varchar(1) DEFAULT NULL,
  `organization` int(11) DEFAULT NULL,
  `crewMember` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `relatedTo` (`relatedTo`),
  KEY `organization` (`organization`),
  KEY `crewMember` (`crewMember`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `PERSON`
--

INSERT INTO `PERSON` (`id`, `relatedTo`, `isTerrorist`, `organization`, `crewMember`) VALUES
('Terrorist', NULL, 'T', NULL, NULL),
('TerroristRelated', 'Terrorist', 'F', NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `SHIP`
--

CREATE TABLE IF NOT EXISTS `SHIP` (
  `id` int(11) NOT NULL,
  `isOfInterest` varchar(1) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `isECMDeployed` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `SHIP_TYPE`
--

CREATE TABLE IF NOT EXISTS `SHIP_TYPE` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(10) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `ELECTRONIC_EQUIPMENT`
--
ALTER TABLE `ELECTRONIC_EQUIPMENT`
  ADD CONSTRAINT `ELECTRONIC_EQUIPMENT_ibfk_2` FOREIGN KEY (`type`) REFERENCES `EQUIPMENT_TYPE` (`id`),
  ADD CONSTRAINT `ELECTRONIC_EQUIPMENT_ibfk_1` FOREIGN KEY (`owner`) REFERENCES `SHIP` (`id`);

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
  ADD CONSTRAINT `PERSON_ibfk_9` FOREIGN KEY (`crewMember`) REFERENCES `SHIP` (`id`) ON DELETE SET NULL ON UPDATE SET NULL,
  ADD CONSTRAINT `PERSON_ibfk_1` FOREIGN KEY (`relatedTo`) REFERENCES `PERSON` (`id`),
  ADD CONSTRAINT `PERSON_ibfk_8` FOREIGN KEY (`organization`) REFERENCES `ORGANIZATION` (`id`) ON DELETE SET NULL ON UPDATE SET NULL;

--
-- Constraints for table `SHIP`
--
ALTER TABLE `SHIP`
  ADD CONSTRAINT `SHIP_ibfk_1` FOREIGN KEY (`type`) REFERENCES `SHIP_TYPE` (`id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;