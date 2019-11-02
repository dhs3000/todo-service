CREATE TABLE todo (
  id SERIAL PRIMARY KEY,
  username VARCHAR (20) NOT NULL,
  title VARCHAR (500) NOT NULL,
  description VARCHAR
);