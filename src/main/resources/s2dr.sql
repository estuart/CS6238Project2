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
  userId INT NOT NULL AUTO_INCREMENT,
  firstName VARCHAR (255) NOT NULL,
  lastName VARCHAR (255) NOT NULL,
  PRIMARY KEY (userId)
);

INSERT INTO s2dr.Users (firstName, lastName) VALUES ('Michael', 'Puckett');
INSERT INTO s2dr.Users (firstName, lastName) VALUES ('Evan', 'Stuart');

CREATE TABLE s2dr.Documents
(
  documentId INT NOT NULL AUTO_INCREMENT,
  documentName VARCHAR (255) NOT NULL,
  contents CLOB NOT NULL,
  securityFlag VARCHAR (255) NOT NULL,
  CONSTRAINT check_security CHECK (securityFlag IN ('NONE', 'INTEGRITY', 'CONFIDENTIALITY')),
  PRIMARY KEY (documentId)
);

CREATE TABLE s2dr.DocumentPermissions
(
  documentId INT NOT NULL,
  clientId VARCHAR (255) NOT NULL,
  permission VARCHAR (5) NOT NULL,
  canPropogate VARCHAR(5) NOT NULL,
  FOREIGN KEY (documentId) REFERENCES s2dr.Documents(documentId),
  FOREIGN KEY (clientId) REFERENCES s2dr.Users(userId)
);
-- TODO constrain DocumentPermissions.permission to be only "READ", "WRITE", "BOTH", or "OWNER"
-- TODO constrain DocumentPermissions.canPropogate to be only "Y" or "N"

