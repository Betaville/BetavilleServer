SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

DROP SCHEMA IF EXISTS `betaville` ;
CREATE SCHEMA IF NOT EXISTS `betaville` ;
USE `betaville`;

-- -----------------------------------------------------
-- Table `betaville`.`user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `betaville`.`user` ;

CREATE  TABLE IF NOT EXISTS `betaville`.`user` (
  `userName` VARCHAR(16) NOT NULL ,
  `userPass` CHAR(32) NOT NULL ,
  `twitterName` VARCHAR(16) NULL ,
  `email` VARCHAR(255) NOT NULL ,
  `activacted` TINYINT(1) NOT NULL DEFAULT false ,
  `bio` VARCHAR(255) NULL ,
  INDEX `userName` (`userName` ASC) ,
  INDEX `userName_2` (`userName` ASC) ,
  PRIMARY KEY (`userName`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`videodesign`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `betaville`.`videodesign` ;

CREATE  TABLE IF NOT EXISTS `betaville`.`videodesign` (
  `designid` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `length` INT NULL ,
  `volume` TINYINT UNSIGNED NULL ,
  `format` ENUM('flv', 'wmv', 'avi', 'mpg') NULL ,
  `directionX` DECIMAL(5,4) NOT NULL ,
  `directionY` DECIMAL(5,4) NOT NULL ,
  `directionZ` DECIMAL(5,4) NOT NULL ,
  PRIMARY KEY (`designid`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`sketchdesign`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `betaville`.`sketchdesign` ;

CREATE  TABLE IF NOT EXISTS `betaville`.`sketchdesign` (
  `designid` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `rotY` SMALLINT NOT NULL ,
  `length` DECIMAL(6,2) NULL ,
  `width` DECIMAL(6,2) NULL ,
  `upPlane` ENUM('X','Y','Z') NOT NULL ,
  PRIMARY KEY (`designid`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`city`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `betaville`.`city` ;

CREATE  TABLE IF NOT EXISTS `betaville`.`city` (
  `cityID` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `cityName` VARCHAR(64) NOT NULL ,
  `state` VARCHAR(64) NOT NULL ,
  `country` VARCHAR(64) NOT NULL ,
  PRIMARY KEY (`cityID`) ,
  INDEX `cityID` (`cityID` ASC) ,
  INDEX `cityID_2` (`cityID` ASC) ,
  INDEX `cityID_3` (`cityID` ASC) )
ENGINE = InnoDB
AUTO_INCREMENT = 2;


-- -----------------------------------------------------
-- Table `betaville`.`coordinate`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `betaville`.`coordinate` ;

CREATE  TABLE IF NOT EXISTS `betaville`.`coordinate` (
  `coordinateID` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `northing` INT NOT NULL ,
  `easting` INT NOT NULL ,
  `latZone` CHAR(1) NOT NULL ,
  `lonZone` TINYINT NOT NULL ,
  `altitude` INT NOT NULL ,
  PRIMARY KEY (`coordinateID`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`design`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `betaville`.`design` ;

CREATE  TABLE IF NOT EXISTS `betaville`.`design` (
  `designID` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(45) NOT NULL ,
  `filepath` VARCHAR(196) NOT NULL ,
  `cityID` INT UNSIGNED NOT NULL ,
  `user` VARCHAR(16) NOT NULL ,
  `coordinateID` INT UNSIGNED NOT NULL ,
  `date` DATETIME NOT NULL ,
  `publicViewing` TINYINT(1) NOT NULL ,
  `description` TEXT NOT NULL ,
  `designURL` TINYTEXT NULL ,
  `designtype` ENUM('audio', 'video', 'model', 'sketch') NOT NULL ,
  `address` VARCHAR(45) NULL ,
  PRIMARY KEY (`designID`) ,
  INDEX `cityID_design` (`cityID` ASC) ,
  INDEX `userfk_design` (`user` ASC) ,
  INDEX `coordinateID_design` (`coordinateID` ASC) ,
  CONSTRAINT `cityID_design`
    FOREIGN KEY (`cityID` )
    REFERENCES `betaville`.`city` (`cityID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `userfk_design`
    FOREIGN KEY (`user` )
    REFERENCES `betaville`.`user` (`userName` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `coordinateID_design`
    FOREIGN KEY (`coordinateID` )
    REFERENCES `betaville`.`coordinate` (`coordinateID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`proposal`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `betaville`.`proposal` ;

CREATE  TABLE IF NOT EXISTS `betaville`.`proposal` (
  `proposalID` INT NOT NULL AUTO_INCREMENT ,
  `sourceID` INT UNSIGNED NOT NULL ,
  `destinationID` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`proposalID`) ,
  INDEX `sourceID` (`sourceID` ASC) ,
  INDEX `destinationID` (`destinationID` ASC) ,
  CONSTRAINT `sourceID`
    FOREIGN KEY (`sourceID` )
    REFERENCES `betaville`.`design` (`designID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `destinationID`
    FOREIGN KEY (`destinationID` )
    REFERENCES `betaville`.`design` (`designID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`audiodesign`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `betaville`.`audiodesign` ;

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
DROP TABLE IF EXISTS `betaville`.`modeldesign` ;

CREATE  TABLE IF NOT EXISTS `betaville`.`modeldesign` (
  `designid` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `rotY` SMALLINT NOT NULL ,
  `length` DECIMAL(6,2) NULL ,
  `width` DECIMAL(6,2) NULL ,
  `height` DECIMAL(6,2) NULL ,
  `textured` TINYINT(1) NOT NULL ,
  PRIMARY KEY (`designid`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`vote`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `betaville`.`vote` ;

CREATE  TABLE IF NOT EXISTS `betaville`.`vote` (
  `voteid` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `voteUp` TINYINT(1) NOT NULL ,
  `user` VARCHAR(16) NOT NULL ,
  `designid` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`voteid`) ,
  INDEX `designidfk_vote` (`designid` ASC) ,
  INDEX `userfk_vote` (`user` ASC) ,
  CONSTRAINT `designidfk_vote`
    FOREIGN KEY (`designid` )
    REFERENCES `betaville`.`design` (`designID` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `userfk_vote`
    FOREIGN KEY (`user` )
    REFERENCES `betaville`.`user` (`userName` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`comment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `betaville`.`comment` ;

CREATE  TABLE IF NOT EXISTS `betaville`.`comment` (
  `commentid` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `designid` INT UNSIGNED NOT NULL ,
  `user` VARCHAR(16) NOT NULL ,
  `comment` TEXT NOT NULL ,
  `date` DATETIME NOT NULL ,
  `spamFlag` TINYINT(1) NOT NULL DEFAULT 0 ,
  `spamVerified` TINYINT(1) NOT NULL DEFAULT 0 ,
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
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `betaville`.`moderators`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `betaville`.`moderators` ;

CREATE  TABLE IF NOT EXISTS `betaville`.`moderators` (
  `user` VARCHAR(16) NOT NULL ,
  `level` TINYINT NOT NULL ,
  PRIMARY KEY (`user`) ,
  INDEX `username` (`user` ASC) ,
  CONSTRAINT `username`
    FOREIGN KEY (`user` )
    REFERENCES `betaville`.`user` (`userName` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
