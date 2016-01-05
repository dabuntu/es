-- phpMyAdmin SQL Dump
-- version 3.4.10.1deb1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jun 09, 2015 at 01:34 PM
-- Server version: 5.5.43
-- PHP Version: 5.3.10-1ubuntu3.18

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `dbeventshop`
--

-- --------------------------------------------------------

--
-- Table structure for table `Alerts`
--

CREATE TABLE IF NOT EXISTS `Alerts` (
  `alert_id` int(11) NOT NULL,
  `alert_name` char(50) NOT NULL,
  `alert_type` varchar(1) NOT NULL,
  `alert_theme` varchar(50) NOT NULL,
  `alert_src` varchar(10) DEFAULT NULL,
  `safe_src` varchar(10) DEFAULT NULL,
  `alert_src_min` int(11) DEFAULT NULL,
  `alert_src_max` int(11) DEFAULT NULL,
  `safe_src_min` int(11) DEFAULT NULL,
  `safe_src_max` int(11) DEFAULT NULL,
  `alert_status` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`alert_id`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



-- --------------------------------------------------------

--
-- Table structure for table `Datasource_Master`
--

CREATE TABLE IF NOT EXISTS `Datasource_Master` (
  `dsmaster_id` int(11) NOT NULL AUTO_INCREMENT,
  `dsmaster_title` varchar(50) NOT NULL,
  `dsmaster_theme` varchar(100) NOT NULL,
  `dsmaster_desc` varchar(250) DEFAULT NULL,
  `dsmaster_url` varchar(500) DEFAULT NULL,
  `dsmaster_format` varchar(30) NOT NULL,
  `dsmaster_type` varchar(30) DEFAULT NULL,
  `dsmaster_creator` int(11) DEFAULT NULL,
  `dsmaster_access` varchar(20) DEFAULT NULL,
  `dsmaster_archive` tinyint(1) DEFAULT NULL,
  `dsmaster_unit` varchar(30) DEFAULT NULL,
  `dsmaster_confident` int(10) DEFAULT NULL,
  `dsmaster_created_date` date NOT NULL,
  `dsmaster_updated_date` date NOT NULL,
  `dsmaster_status` char(1) DEFAULT NULL,
  PRIMARY KEY (`dsmaster_id`),
  KEY `Datasource_Master_ibfk_1` (`dsmaster_creator`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=9 ;


-- --------------------------------------------------------

--
-- Table structure for table `Datasource_Resolution`
--

CREATE TABLE IF NOT EXISTS `Datasource_Resolution` (
  `dsresolution_id` int(11) NOT NULL AUTO_INCREMENT,
  `dsmaster_id` int(11) DEFAULT NULL,
  `datastream_name` varchar(50) DEFAULT NULL,
  `time_window` int(10) DEFAULT NULL,
  `latitude_unit` double(15,5) DEFAULT NULL,
  `longitude_unit` double(15,5) DEFAULT NULL,
  `boundingbox` varchar(50) NOT NULL,
  `regrid_function` varchar(50) DEFAULT NULL,
  `resolution_type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`dsresolution_id`),
  KEY `dsmaster_id` (`dsmaster_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=9 ;


--
-- Table structure for table `Query_Master`
--

CREATE TABLE IF NOT EXISTS `Query_Master` (
  `query_id` int(11) NOT NULL AUTO_INCREMENT,
  `query_creator_id` int(10) DEFAULT NULL,
  `query_name` varchar(50) DEFAULT NULL,
  `query_desc` varchar(50) DEFAULT NULL,
  `query_esql` text,
  `time_window` int(10) NOT NULL,
  `latitude_unit` double(15,5) NOT NULL,
  `longitude_unit` double(15,5) NOT NULL,
  `boundingbox` varchar(50) NOT NULL,
  `query_status` char(1) NOT NULL,
  `qid_parent` int(11) DEFAULT NULL,
  PRIMARY KEY (`query_id`),
  KEY `qid_parent` (`qid_parent`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=45 ;


--
-- Table structure for table `tbl_Role_Master`
--

CREATE TABLE IF NOT EXISTS `tbl_Role_Master` (
  `role_id` int(11) NOT NULL DEFAULT '0',
  `role_type` varchar(15) NOT NULL,
  `role_desc` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `tbl_Role_Master`
--

INSERT INTO `tbl_Role_Master` (`role_id`, `role_type`, `role_desc`) VALUES
(1, 'Admin', NULL),
(2, 'User', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `tbl_User_Master`
--

CREATE TABLE IF NOT EXISTS `tbl_User_Master` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_email` varchar(35) NOT NULL,
  `user_password` varchar(150) DEFAULT NULL,
  `user_fullname` varchar(50) NOT NULL,
  `user_gender` char(1) NOT NULL,
  `user_status` char(1) DEFAULT NULL,
  `user_authen_key` varchar(50) DEFAULT NULL,
  `user_role_id` int(11) NOT NULL,
  `user_created_date` date NOT NULL,
  `user_last_accessd` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  KEY `role_fk` (`user_role_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=81 ;



--
-- Table structure for table `Wrapper`
--

CREATE TABLE IF NOT EXISTS `Wrapper` (
  `wrapper_id` int(11) NOT NULL AUTO_INCREMENT,
  `wrapper_name` varchar(50) NOT NULL,
  `wrapper_type` varchar(10) DEFAULT NULL,
  `wrapper_key_value` varchar(255) DEFAULT NULL,
  `bag_of_words` varchar(100) DEFAULT NULL,
  `visual_mask_mat` blob,
  `visual_ignore_since` int(10) DEFAULT NULL,
  `archive_start_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `archive_end_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `archive_gen_rate` bigint(20) DEFAULT NULL,
  `visual_color_mat` blob,
  `visual_tran_mat` blob,
  `csv_file_url` varchar(250) DEFAULT NULL,
  `dsmaster_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`wrapper_id`),
  KEY `dsmaster_id` (`dsmaster_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=10 ;



--
-- Constraints for dumped tables
--

--
-- Constraints for table `Alerts`
--
ALTER TABLE `Alerts`
  ADD CONSTRAINT `Alerts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `tbl_User_Master` (`user_id`);

--
-- Constraints for table `Datasource_Master`
--
ALTER TABLE `Datasource_Master`
  ADD CONSTRAINT `Datasource_Master_ibfk_1` FOREIGN KEY (`dsmaster_creator`) REFERENCES `tbl_User_Master` (`user_id`);

--
-- Constraints for table `Datasource_Resolution`
--
ALTER TABLE `Datasource_Resolution`
  ADD CONSTRAINT `Datasource_Resolution_ibfk_1` FOREIGN KEY (`dsmaster_id`) REFERENCES `Datasource_Master` (`dsmaster_id`);

--
-- Constraints for table `Query_Datasource`
--
ALTER TABLE `Query_Datasource`
  ADD CONSTRAINT `query_datasource_ibfk_1` FOREIGN KEY (`query_id`) REFERENCES `Query_Master` (`query_id`),
  ADD CONSTRAINT `query_datasource_ibfk_2` FOREIGN KEY (`datasource_id`) REFERENCES `Datasource_Master` (`dsmaster_id`);

--
-- Constraints for table `Query_Master`
--
ALTER TABLE `Query_Master`
  ADD CONSTRAINT `query_master_ibfk_1` FOREIGN KEY (`qid_parent`) REFERENCES `Query_Master` (`query_id`);

--
-- Constraints for table `tbl_User_Master`
--
ALTER TABLE `tbl_User_Master`
  ADD CONSTRAINT `role_fk` FOREIGN KEY (`user_role_id`) REFERENCES `tbl_Role_Master` (`role_id`);

--
-- Constraints for table `Wrapper`
--
ALTER TABLE `Wrapper`
  ADD CONSTRAINT `Wrapper_ibfk_2` FOREIGN KEY (`dsmaster_id`) REFERENCES `Datasource_Master` (`dsmaster_id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;