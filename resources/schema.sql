CREATE USER 'scott'@'localhost' IDENTIFIED BY 'tiger';
GRANT ALL PRIVILEGES ON 'carracing' . * TO 'scott'@'localhost';

CREATE DATABASE IF NOT EXISTS carracing;

use carracing;

CREATE TABLE race_summary_user (
  user_id         int(10) NOT NULL, 
  profit          float(10, 5) NOT NULL, 
  race_summary_id int(10) NOT NULL, 
  PRIMARY KEY (user_id, race_summary_id));

CREATE TABLE race_summary (
  race_id       int(10) NOT NULL, 
  winner        int(10) NOT NULL, 
  system_profit float(10, 5) NOT NULL, 
  total_bets    int(10) NOT NULL,
  amount_bets   int(10) NOT NULL,
  id            int(10) NOT NULL AUTO_INCREMENT, 
  PRIMARY KEY (id));

CREATE TABLE race (
  id       int(10) NOT NULL AUTO_INCREMENT, 
  name     varchar(255) NOT NULL, 
  started  varchar(255), 
  finished varchar(255), 
  sound_id int(10), 
  status   char(10), 
  PRIMARY KEY (id));

CREATE TABLE bet (
  id      int(10) NOT NULL AUTO_INCREMENT, 
  amount  int(10) NOT NULL, 
  user_id int(10) NOT NULL, 
  car_id  int(10) NOT NULL, 
  PRIMARY KEY (id));

CREATE TABLE `user` (
  id       int(10) NOT NULL AUTO_INCREMENT, 
  fullname varchar(255) NOT NULL, 
  login    varchar(255) NOT NULL UNIQUE, 
  password varchar(255) NOT NULL, 
  PRIMARY KEY (id));

CREATE TABLE car (
  id       int(10) NOT NULL AUTO_INCREMENT, 
  name     varchar(255) NOT NULL, 
  speed    int(10), 
  type     varchar(100), 
  color    char(10), 
  shape    varchar(100), 
  `size`   varchar(100), 
  distance float(10, 5), 
  race_id   int(10) NOT NULL, 
  PRIMARY KEY (id));

ALTER TABLE race_summary ADD INDEX FKrace_summa394860 (winner), ADD CONSTRAINT FKrace_summa394860 FOREIGN KEY (winner) REFERENCES car (id);
ALTER TABLE race_summary ADD INDEX FKrace_summa508153 (race_id), ADD CONSTRAINT FKrace_summa508153 FOREIGN KEY (race_id) REFERENCES race (id);
ALTER TABLE race_summary_user ADD INDEX FKrace_summa572660 (race_summary_id), ADD CONSTRAINT FKrace_summa572660 FOREIGN KEY (race_summary_id) REFERENCES race_summary (id);
ALTER TABLE race_summary_user ADD INDEX FKrace_summa574829 (user_id), ADD CONSTRAINT FKrace_summa574829 FOREIGN KEY (user_id) REFERENCES `user` (id);
ALTER TABLE car ADD INDEX FKcar19579 (race_id), ADD CONSTRAINT FKcar19579 FOREIGN KEY (race_id) REFERENCES race (id);
ALTER TABLE bet ADD INDEX FKbet330659 (user_id), ADD CONSTRAINT FKbet330659 FOREIGN KEY (user_id) REFERENCES `user` (id);
ALTER TABLE bet ADD INDEX FKbet630349 (car_id), ADD CONSTRAINT FKbet630349 FOREIGN KEY (car_id) REFERENCES car (id);


CREATE VIEW race_report AS 
  SELECT 
    CONCAT(r.name, ' ', r.id) AS 'race_name', 
    rs.total_bets, rs.amount_bets, rs.system_profit, 
    CONCAT(c.type, '-', c.id, '-', r.id) AS 'car_name'
  FROM race r, race_summary rs, car c
  WHERE r.id = rs.race_id && rs.winner = c.id