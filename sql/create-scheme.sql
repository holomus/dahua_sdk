DROP USER IF EXISTS dahua_user;

CREATE USER dahua_user WITH PASSWORD 'dahua_secret';

GRANT ALL PRIVILEGES ON DATABASE dahua_user TO dahua_user;
