/*
 * This is the script that sets up our H2 in-memory database. Since the database is
 * stored in memory per servlet "instance", any data that is added will not persist
 * between sessions. As a result, anything that we want to be truly persistent across
 * servlet contexts and machines must be manually added in this file. This includes
 * the schema as well as any data we want stored.
 */
CREATE SCHEMA s2dr;

-- Table that will hold "Users" of the system
CREATE TABLE s2dr.Users
(
  userId INT NOT NULL,
  firstName VARCHAR (255) NOT NULL,
  lastName VARCHAR (255) NOT NULL,
  PRIMARY KEY (userId)
);

INSERT INTO s2dr.Users VALUES (1234, 'Michael', 'Puckett');

