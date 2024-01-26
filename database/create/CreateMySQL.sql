--
-- Script for MySQL 8+ or Maria DB 10.4.10
--
-- Will only create the Data base with its tables.
-- 14/02/2022

-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema lunchtime
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `lunchtime` ;

-- -----------------------------------------------------
-- Schema lunchtime
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `lunchtime` DEFAULT CHARACTER SET utf8 ;
USE `lunchtime` ;

-- -----------------------------------------------------
-- Table `ltimage`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ltimage` ;

CREATE TABLE IF NOT EXISTS `ltimage` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `image_64` TEXT NULL,
  `image_path` VARCHAR(500) NULL,
  `is_default` TINYINT ZEROFILL NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ltuser`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ltuser` ;

CREATE TABLE IF NOT EXISTS `ltuser` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `status` TINYINT NOT NULL COMMENT 'ENABLED(0), DISABLED(1), DELETED(2);\n',
  `password` VARCHAR(1024) NOT NULL,
  `email` VARCHAR(300) NOT NULL,
  `sex` TINYINT NULL COMMENT 'MAN(0), WOMAN(1), OTHER(2)',
  `name` VARCHAR(256) NULL COMMENT 'Nom de famille',
  `firstname` VARCHAR(256) NULL COMMENT 'Prénom',
  `wallet` DECIMAL(5,2) NULL,
  `registration_date` DATETIME NULL,
  `phone` VARCHAR(15) NULL,
  `address` VARCHAR(500) NULL,
  `postal_code` VARCHAR(10) NULL,
  `town` VARCHAR(150) NULL,
  `image_id` INT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE,
  INDEX `fk_LTUser_LTImage1_idx` (`image_id` ASC) VISIBLE,
  CONSTRAINT `fk_LTUser_LTImage1`
    FOREIGN KEY (`image_id`)
    REFERENCES `ltimage` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ltmeal`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ltmeal` ;

CREATE TABLE IF NOT EXISTS `ltmeal` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `status` TINYINT NOT NULL COMMENT 'ENABLED(0), DISABLED(1),DELETED(2);',
  `label` VARCHAR(200) NOT NULL,
  `category` TINYINT NULL,
  `description` TEXT NULL,
  `price_df` DECIMAL(5,2) NULL COMMENT 'Duty Free',
  `available_for_weeks_and_days` TEXT NULL COMMENT 'Numero des semaines et jour dans la semaine pour lequel le menu est disponible.\n{[{\"week\":1, \"day\":1},{\"week\":45}]}\nSi pas de jour alors disponible toute la semaine.\nSi totalement null alors disponible tous les jours de toutes les semaines.',
  `image_id` INT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_LTMeal_LTImage1_idx` (`image_id` ASC) VISIBLE,
  CONSTRAINT `fk_LTMeal_LTImage1`
    FOREIGN KEY (`image_id`)
    REFERENCES `ltimage` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ltmenu`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ltmenu` ;

CREATE TABLE IF NOT EXISTS `ltmenu` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `status` TINYINT NOT NULL COMMENT 'ENABLED(0), DISABLED(1),DELETED(2);',
  `label` VARCHAR(200) NOT NULL,
  `description` TEXT NULL,
  `price_df` DECIMAL(5,2) NULL COMMENT 'Duty Free',
  `available_for_weeks_and_days` TEXT NULL COMMENT 'Numero des semaines et jour dans la semaine pour lequel le menu est disponible.\n{[{\"week\":1, \"day\":1},{\"week\":45}]}\nSi pas de jour alors disponible toute la semaine.\nSi totalement null alors disponible tous les jours de toutes les semaines.',
  `image_id` INT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_LTMenu_LTImage1_idx` (`image_id` ASC) VISIBLE,
  CONSTRAINT `fk_LTMenu_LTImage1`
    FOREIGN KEY (`image_id`)
    REFERENCES `ltimage` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ltmenu_has_meal`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ltmenu_has_meal` ;

CREATE TABLE IF NOT EXISTS `ltmenu_has_meal` (
  `menu_id` INT NOT NULL,
  `meal_id` INT NOT NULL,
  PRIMARY KEY (`menu_id`, `meal_id`),
  INDEX `fk_mhm1_idx` (`meal_id` ASC) VISIBLE,
  INDEX `fk_mhm2_idx` (`menu_id` ASC) VISIBLE,
  CONSTRAINT `fk_mhm_menu`
    FOREIGN KEY (`menu_id`)
    REFERENCES `ltmenu` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_mhm_meal`
    FOREIGN KEY (`meal_id`)
    REFERENCES `ltmeal` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ltorder`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ltorder` ;

CREATE TABLE IF NOT EXISTS `ltorder` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `status` TINYINT NULL COMMENT 'CREATED(0), DELIVERED(1),CANCELED(2);',
  `user_id` INT NOT NULL,
  `creation_date` DATE NULL,
  `creation_time` TIME NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_commande_utilisateur1_idx` (`user_id` ASC) VISIBLE,
  CONSTRAINT `fk_commande_utilisateur1`
    FOREIGN KEY (`user_id`)
    REFERENCES `ltuser` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ltquantity`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ltquantity` ;

CREATE TABLE IF NOT EXISTS `ltquantity` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `meal_id` INT NULL,
  `menu_id` INT NULL,
  `quantity` INT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_commandeHasPlat_plat1_idx` (`meal_id` ASC) VISIBLE,
  INDEX `fk_ltquantity_ltmenu1_idx` (`menu_id` ASC) VISIBLE,
  CONSTRAINT `fk_qm_meal`
    FOREIGN KEY (`meal_id`)
    REFERENCES `ltmeal` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_ltquantity_ltmenu1`
    FOREIGN KEY (`menu_id`)
    REFERENCES `ltmenu` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ltingredient`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ltingredient` ;

CREATE TABLE IF NOT EXISTS `ltingredient` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `status` TINYINT NOT NULL COMMENT 'ENABLED(0), DISABLED(1),DELETED(2);',
  `label` VARCHAR(200) NOT NULL,
  `description` TEXT NULL,
  `image_id` INT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_LTIngredient_LTImage1_idx` (`image_id` ASC) VISIBLE,
  CONSTRAINT `fk_LTIngredient_LTImage1`
    FOREIGN KEY (`image_id`)
    REFERENCES `ltimage` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ltmeal_has_ingredient`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ltmeal_has_ingredient` ;

CREATE TABLE IF NOT EXISTS `ltmeal_has_ingredient` (
  `meal_id` INT NOT NULL,
  `ingredient_id` INT NOT NULL,
  PRIMARY KEY (`meal_id`, `ingredient_id`),
  INDEX `fk_mhi_1_idx` (`ingredient_id` ASC) VISIBLE,
  INDEX `fk_mhi_2_idx` (`meal_id` ASC) VISIBLE,
  CONSTRAINT `fk_mhi_meal`
    FOREIGN KEY (`meal_id`)
    REFERENCES `ltmeal` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_mhi_ingredient`
    FOREIGN KEY (`ingredient_id`)
    REFERENCES `ltingredient` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ltconstraint`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ltconstraint` ;

CREATE TABLE IF NOT EXISTS `ltconstraint` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `order_time_limit` TIME NOT NULL,
  `maximum_order_per_day` INT NOT NULL,
  `rate_vat` DECIMAL(5,2) NOT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ltorder_has_quantity`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ltorder_has_quantity` ;

CREATE TABLE IF NOT EXISTS `ltorder_has_quantity` (
  `order_id` INT NOT NULL,
  `quantity_id` INT NOT NULL,
  PRIMARY KEY (`order_id`, `quantity_id`),
  INDEX `fk_ohq_1_idx` (`quantity_id` ASC) VISIBLE,
  INDEX `fk_ohq_2_idx` (`order_id` ASC) VISIBLE,
  CONSTRAINT `fk_ohq_order`
    FOREIGN KEY (`order_id`)
    REFERENCES `ltorder` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_ohq_quantityMeal`
    FOREIGN KEY (`quantity_id`)
    REFERENCES `ltquantity` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ltrole`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ltrole` ;

CREATE TABLE IF NOT EXISTS `ltrole` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `label` VARCHAR(200) NOT NULL COMMENT 'ROLE_USER or ROLE_LUNCHLADY',
  `description` TEXT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_role_user1` (`user_id` ASC) VISIBLE,
  CONSTRAINT `fk_role_user1`
    FOREIGN KEY (`user_id`)
    REFERENCES `ltuser` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
