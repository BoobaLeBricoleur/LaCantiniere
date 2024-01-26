-- Il faudra potentiellement mettre les deux lignes suivantes en commentaire 
USE master;
CREATE DATABASE lunchtime;

DROP TABLE IF EXISTS ltconstraint;
DROP TABLE IF EXISTS ltmeal_has_ingredient;
DROP TABLE IF EXISTS ltmenu_has_meal;
DROP TABLE IF EXISTS ltorder_has_quantity;
DROP TABLE IF EXISTS ltquantity;
DROP TABLE IF EXISTS ltmeal;
DROP TABLE IF EXISTS ltorder;
DROP TABLE IF EXISTS ltingredient;
DROP TABLE IF EXISTS ltmenu;
DROP TABLE IF EXISTS ltrole;
DROP TABLE IF EXISTS ltuser;
DROP TABLE IF EXISTS ltimage;

CREATE TABLE dbo.ltimage (
  id INT PRIMARY KEY IDENTITY(1, 1),
  image_64 TEXT,
  image_path VARCHAR(500),
  is_default SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE dbo.ltconstraint (
  id INT PRIMARY KEY IDENTITY(1, 1),
  order_time_limit TIME NOT NULL,
  maximum_order_per_day INTEGER NOT NULL,
  rate_vat DECIMAL(5,2) NOT NULL  
);

CREATE TABLE dbo.ltingredient (
  id INT PRIMARY KEY IDENTITY(1, 1),
  status SMALLINT NOT NULL,
  label VARCHAR(200) NOT NULL,
  description TEXT,
  image_id INT,
  FOREIGN KEY (image_id) REFERENCES dbo.ltimage (id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE dbo.ltmeal (
  id INT PRIMARY KEY IDENTITY(1, 1),
  status SMALLINT NOT NULL,
  label VARCHAR(200) NOT NULL,
  category SMALLINT,
  description TEXT,
  price_df DECIMAL(5,2),
  available_for_weeks_and_days TEXT,
  image_id INT,
  FOREIGN KEY (image_id) REFERENCES dbo.ltimage (id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE dbo.ltmeal_has_ingredient (
  meal_id INTEGER NOT NULL,
  ingredient_id INTEGER NOT NULL,
  PRIMARY KEY (meal_id, ingredient_id),

  FOREIGN KEY (meal_id) REFERENCES dbo.ltmeal (id) ON DELETE NO ACTION ON UPDATE NO ACTION,
  FOREIGN KEY (ingredient_id) REFERENCES dbo.ltingredient (id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE dbo.ltmenu (
  id INT PRIMARY KEY IDENTITY(1, 1),
  status SMALLINT NOT NULL,
  label VARCHAR(200) NOT NULL,
  description TEXT,
  price_df DECIMAL(5,2),
  available_for_weeks_and_days TEXT,
  image_id INT,
  FOREIGN KEY (image_id) REFERENCES dbo.ltimage (id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE dbo.ltmenu_has_meal (
  menu_id INTEGER NOT NULL,
  meal_id INTEGER NOT NULL,
  PRIMARY KEY (menu_id,meal_id),

  FOREIGN KEY (menu_id) REFERENCES dbo.ltmenu (id) ON DELETE NO ACTION ON UPDATE NO ACTION,
  FOREIGN KEY (meal_id) REFERENCES dbo.ltmeal (id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE dbo.ltuser (
  id INT PRIMARY KEY IDENTITY(1, 1),
  status SMALLINT NOT NULL,
  password VARCHAR(1024) NOT NULL,
  email VARCHAR(300) NOT NULL,
  sex SMALLINT,
  name VARCHAR(256),
  firstname VARCHAR(256),
  wallet DECIMAL(5,2),
  registration_date DATETIME,
  phone VARCHAR(15),
  address VARCHAR(500),
  postal_code VARCHAR(10),
  town VARCHAR(150),
  image_id INT,

  FOREIGN KEY (image_id) REFERENCES dbo.ltimage (id) ON DELETE NO ACTION ON UPDATE NO ACTION,
  UNIQUE (email)
);

CREATE TABLE dbo.ltorder (
  id INT PRIMARY KEY IDENTITY(1, 1),
  status SMALLINT,
  user_id INTEGER NOT NULL,
  creation_date DATE,
  creation_time TIME,  

  FOREIGN KEY (user_id) REFERENCES dbo.ltuser (id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE dbo.ltquantity (
  id INT PRIMARY KEY IDENTITY(1, 1),
  meal_id INTEGER,
  menu_id INTEGER,
  quantity INTEGER,

  FOREIGN KEY (meal_id) REFERENCES dbo.ltmeal (id) ON DELETE NO ACTION ON UPDATE NO ACTION,
  FOREIGN KEY (menu_id) REFERENCES dbo.ltmenu (id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE dbo.ltorder_has_quantity (
  order_id INTEGER NOT NULL,
  quantity_id INTEGER NOT NULL,
  PRIMARY KEY (order_id,quantity_id),
  
  FOREIGN KEY (order_id) REFERENCES dbo.ltorder (id) ON DELETE NO ACTION ON UPDATE NO ACTION,
  FOREIGN KEY (quantity_id) REFERENCES dbo.ltquantity (id) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE dbo.ltrole (
  id INT PRIMARY KEY IDENTITY(1, 1),
  user_id INTEGER NOT NULL,
  label VARCHAR(200) NOT NULL,
  description TEXT,
  FOREIGN KEY (user_id) REFERENCES dbo.ltuser (id) ON DELETE NO ACTION ON UPDATE NO ACTION
);