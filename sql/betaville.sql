SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `betaville` ;
USE `betaville` ;

-- -----------------------------------------------------
-- Table `betaville`.`user`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `betaville`.`user` (
  `userName` VARCHAR(255) NOT NULL ,
  `userPass` CHAR(32) NULL ,
  `twitterName` VARCHAR(16) NULL DEFAULT NULL ,
  `email` VARCHAR(255) NOT NULL ,
  `activated` TINYINT(1) NOT NULL DEFAULT '0' ,
  `bio` VARCHAR(255) NULL DEFAULT NULL ,
  `showEmail` TINYINT(1) NOT NULL DEFAULT 0 ,
  `website` TINYTEXT NULL DEFAULT NULL ,
  `type` ENUM('member', 'base_committer', 'data_searcher', 'moderator', 'admin') NULL DEFAULT 'member' ,
  `displayName` VARCHAR(45) NULL ,
  `strongpass` CHAR(40) NOT NULL ,
  `strongsalt` CHAR(12) NOT NULL ,
  `confirmcode` CHAR(32) NOT NULL DEFAULT 'registered_via_app' ,
  PRIMARY KEY (`userName`) ,
  INDEX `userName` (`userName` ASC) ,
  INDEX `userName_2` (`userName` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;


-- -----------------------------------------------------
-- Table `betaville`.`videodesign`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `betaville`.`videodesign` (
  `designid` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `length` INT(11) NULL DEFAULT NULL ,
  `volume` TINYINT(3) UNSIGNED NULL DEFAULT NULL ,
  `format` ENUM('flv','wmv','avi','mpg') NULL DEFAULT NULL ,
  `directionX` DECIMAL(5,4) NOT NULL ,
  `directionY` DECIMAL(5,4) NOT NULL ,
  `directionZ` DECIMAL(5,4) NOT NULL ,
  PRIMARY KEY (`designid`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;


-- -----------------------------------------------------
-- Table `betaville`.`sketchdesign`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `betaville`.`sketchdesign` (
  `designid` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `rotY` FLOAT NOT NULL ,
  `length` DECIMAL(6,2) NULL ,
  `width` DECIMAL(6,2) NULL ,
  `upPlane` ENUM('X','Y','Z') NOT NULL ,
  PRIMARY KEY (`designid`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`city`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `betaville`.`city` (
  `cityID` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `cityName` VARCHAR(64) NOT NULL ,
  `state` VARCHAR(64) NOT NULL ,
  `country` VARCHAR(64) NOT NULL ,
  PRIMARY KEY (`cityID`) ,
  INDEX `cityID` (`cityID` ASC) ,
  INDEX `cityID_2` (`cityID` ASC) ,
  INDEX `cityID_3` (`cityID` ASC) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`coordinate`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `betaville`.`coordinate` (
  `coordinateID` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `northing` INT NOT NULL ,
  `northingCM` INT NOT NULL ,
  `easting` INT NOT NULL ,
  `eastingCM` INT NOT NULL ,
  `altitude` FLOAT NOT NULL ,
  `latZone` CHAR(1)  NOT NULL ,
  `lonZone` TINYINT NOT NULL ,
  PRIMARY KEY (`coordinateID`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`design`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `betaville`.`design` (
  `designID` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(128) NOT NULL ,
  `filepath` VARCHAR(196) NOT NULL ,
  `cityID` INT(10) UNSIGNED NOT NULL ,
  `user` VARCHAR(255) NOT NULL ,
  `coordinateID` INT(10) UNSIGNED NOT NULL ,
  `date` DATETIME NOT NULL ,
  `publicViewing` TINYINT(1) NOT NULL ,
  `description` TEXT NOT NULL ,
  `designURL` TINYTEXT NULL DEFAULT NULL ,
  `designtype` ENUM('audio','video','model','sketch', 'empty') NOT NULL ,
  `address` VARCHAR(128) NULL DEFAULT NULL ,
  `isAlive` TINYINT(1) NOT NULL DEFAULT '1' ,
  `favelist` MEDIUMTEXT NULL ,
  `lastModified` DATETIME NULL ,
  PRIMARY KEY (`designID`) ,
  INDEX `cityID_design` (`cityID` ASC) ,
  INDEX `userfk_design` (`user` ASC) ,
  INDEX `coordinateID_design` (`coordinateID` ASC) ,
  CONSTRAINT `cityID_design`
    FOREIGN KEY (`cityID` )
    REFERENCES `betaville`.`city` (`cityID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `coordinateID_design`
    FOREIGN KEY (`coordinateID` )
    REFERENCES `betaville`.`coordinate` (`coordinateID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `userfk_design`
    FOREIGN KEY (`user` )
    REFERENCES `betaville`.`user` (`userName` )
    ON DELETE NO ACTION
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;


-- -----------------------------------------------------
-- Table `betaville`.`proposal`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `betaville`.`proposal` (
  `proposalID` INT(11) NOT NULL AUTO_INCREMENT ,
  `sourceID` INT(10) UNSIGNED NOT NULL ,
  `destinationID` INT(10) UNSIGNED NOT NULL ,
  `type` ENUM('proposal','version') NOT NULL ,
  `removables` TEXT NOT NULL ,
  `level` ENUM('closed','group','all') NULL ,
  `user_group` TEXT NULL ,
  `featured` TINYINT UNSIGNED NULL ,
  PRIMARY KEY (`proposalID`) ,
  INDEX `sourceID` (`sourceID` ASC) ,
  INDEX `destinationID` (`destinationID` ASC) ,
  CONSTRAINT `destinationID`
    FOREIGN KEY (`destinationID` )
    REFERENCES `betaville`.`design` (`designID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `sourceID`
    FOREIGN KEY (`sourceID` )
    REFERENCES `betaville`.`design` (`designID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 5
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;


-- -----------------------------------------------------
-- Table `betaville`.`audiodesign`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `betaville`.`audiodesign` (
  `designid` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `length` INT NULL ,
  `volume` TINYINT NULL ,
  `directionX` DECIMAL(5,4) NOT NULL ,
  `directionY` DECIMAL(5,4) NOT NULL ,
  `directionZ` DECIMAL(5,4) NOT NULL ,
  PRIMARY KEY (`designid`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`modeldesign`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `betaville`.`modeldesign` (
  `designid` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `rotY` FLOAT NOT NULL ,
  `length` DECIMAL(6,2) NULL ,
  `width` DECIMAL(6,2) NULL ,
  `height` DECIMAL(6,2) NULL ,
  `textured` TINYINT(1)  NOT NULL ,
  `rotX` FLOAT NULL ,
  `rotZ` FLOAT NULL ,
  PRIMARY KEY (`designid`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`comment`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `betaville`.`comment` (
  `commentid` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `designid` INT UNSIGNED NOT NULL ,
  `user` VARCHAR(255) NOT NULL ,
  `comment` TEXT NOT NULL ,
  `date` DATETIME NOT NULL ,
  `spamFlag` TINYINT(1)  NOT NULL DEFAULT 0 ,
  `spamVerified` TINYINT(1)  NOT NULL DEFAULT 0 ,
  `repliesTo` INT NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`commentid`) ,
  INDEX `designidfk_comment` (`designid` ASC) ,
  INDEX `userfk_comment` (`user` ASC) ,
  CONSTRAINT `designidfk_comment`
    FOREIGN KEY (`designid` )
    REFERENCES `betaville`.`design` (`designID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `userfk_comment`
    FOREIGN KEY (`user` )
    REFERENCES `betaville`.`user` (`userName` )
    ON DELETE NO ACTION
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`country`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `betaville`.`country` (
  `name` CHAR(255) NOT NULL )
ENGINE = MyISAM
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;


-- -----------------------------------------------------
-- Table `betaville`.`emptydesign`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `betaville`.`emptydesign` (
  `designid` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `length` DECIMAL(6,2) NULL DEFAULT NULL ,
  `width` DECIMAL(6,2) NULL DEFAULT NULL ,
  PRIMARY KEY (`designid`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = latin1
COLLATE = latin1_swedish_ci;


-- -----------------------------------------------------
-- Table `betaville`.`session`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `betaville`.`session` (
  `sessionID` INT NOT NULL AUTO_INCREMENT ,
  `userName` VARCHAR(255) NOT NULL ,
  `timeEntered` DATETIME NULL ,
  `timeLeft` DATETIME NULL ,
  PRIMARY KEY (`sessionID`) ,
  INDEX `session_user_fk` (`userName` ASC) ,
  CONSTRAINT `session_user_fk`
    FOREIGN KEY (`userName` )
    REFERENCES `betaville`.`user` (`userName` )
    ON DELETE NO ACTION
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`wormhole`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `betaville`.`wormhole` (
  `wormholeid` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `coordinateid` INT UNSIGNED NOT NULL ,
  `cityid` INT UNSIGNED NOT NULL ,
  `name` TINYTEXT NOT NULL ,
  `isAlive` TINYINT UNSIGNED NOT NULL ,
  PRIMARY KEY (`wormholeid`) ,
  INDEX `wormhole_coordinate_fk` (`coordinateid` ASC) ,
  INDEX `wormhole_city_fk` (`cityid` ASC) ,
  CONSTRAINT `wormhole_coordinate_fk`
    FOREIGN KEY (`coordinateid` )
    REFERENCES `betaville`.`coordinate` (`coordinateID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `wormhole_city_fk`
    FOREIGN KEY (`cityid` )
    REFERENCES `betaville`.`city` (`cityID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`live_sessions`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `betaville`.`live_sessions` (
  `session_token` CHAR(40) NULL ,
  `user` TINYTEXT NULL ,
  `ip_address` VARCHAR(15) NULL ,
  `session_start` DATETIME NULL ,
  `last_touched` DATETIME NULL )
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
