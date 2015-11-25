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
  userName VARCHAR (255) NOT NULL,
  signature BLOB NOT NULL,
  PRIMARY KEY (userId)
);

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
  userId INT NOT NULL,
  permission VARCHAR (5) NOT NULL,
  canPropogate VARCHAR(5) NOT NULL,
  FOREIGN KEY (documentId) REFERENCES s2dr.Documents(documentId),
  FOREIGN KEY (userId) REFERENCES s2dr.Users(userId)
);
-- TODO constrain DocumentPermissions.permission to be only "READ", "WRITE", "BOTH", or "OWNER"
-- TODO constrain DocumentPermissions.canPropogate to be only "Y" or "N"

