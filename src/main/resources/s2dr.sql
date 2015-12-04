CREATE SCHEMA s2dr;

-- Table that will hold "Users" of the system
CREATE TABLE s2dr.Users
(
  userName VARCHAR (255) NOT NULL,
  signature BLOB NOT NULL,
  pubKeyModulus BLOB NOT NULL,
  pubKeyExponent BLOB NOT NULL,
  PRIMARY KEY (userName)
);

CREATE TABLE s2dr.Documents
(
  documentName VARCHAR (255) NOT NULL,
  contents BLOB NOT NULL,
  uploadUser VARCHAR(255) NOT NULL,
  encryptionKey BLOB,
  signature BLOB,
  PRIMARY KEY (documentName),
  FOREIGN KEY (uploadUser) REFERENCES s2dr.Users(userName)
);

CREATE TABLE s2dr.DocumentSecurity
(
  documentName VARCHAR (255) NOT NULL,
  securityFlag VARCHAR (255) NOT NULL,
  CONSTRAINT check_security CHECK (securityFlag IN ('NONE', 'INTEGRITY', 'CONFIDENTIALITY')),
  PRIMARY KEY (documentName, securityFlag),
  FOREIGN KEY (documentName) REFERENCES s2dr.Documents(documentName)
);

CREATE TABLE s2dr.DocumentPermissions
(
  documentName VARCHAR (255) NOT NULL,
  userName VARCHAR(255) NOT NULL,
  permission VARCHAR (5) NOT NULL,
  CONSTRAINT check_permission CHECK (permission IN ('READ', 'WRITE', 'OWNER')),
  timeLimit TIMESTAMP,
  canPropogate VARCHAR (5) NOT NULL,
  CONSTRAINT check_bool CHECK (canPropogate IN ('TRUE', 'FALSE')),
  FOREIGN KEY (documentName) REFERENCES s2dr.Documents(documentName)
  -- we cannot set PRIMARY KEY(documentName, userName, permission) because we do not
    -- delete permissions that have expired based on time. These permissions aren't
    -- deleted because we ignore them in the permission SELECT query
  -- we cannot have a foreign key for Users.userName because we allow "ALL"
);

