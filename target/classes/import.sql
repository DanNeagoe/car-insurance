INSERT INTO owner (id, name, email) VALUES (1, 'Ana Pop', 'ana.pop@example.com');
INSERT INTO owner (id, name, email) VALUES (2, 'Bogdan Ionescu', 'bogdan.ionescu@example.com');

INSERT INTO car (id, vin, make, model, year_of_manufacture, owner_id) VALUES (1, 'VIN12345', 'Dacia', 'Logan', 2018, 1);
INSERT INTO car (id, vin, make, model, year_of_manufacture, owner_id) VALUES (2, 'VIN67890', 'VW', 'Golf', 2021, 2);

INSERT INTO insurancepolicy (id, car_id, provider, start_date, end_date) VALUES (1, 1, 'Allianz', DATE '2024-01-01', DATE '2024-12-31');
INSERT INTO insurancepolicy (id, car_id, provider, start_date, end_date) VALUES (2, 1, 'Groupama', DATE '2025-01-01', DATE '2025-12-31');
INSERT INTO insurancepolicy (id, car_id, provider, start_date, end_date) VALUES (3, 2, 'Allianz', DATE '2025-03-01', DATE '2025-09-30');

-- claims test
INSERT INTO claims (id, car_id, claim_date, description, amount) VALUES (1, 1, DATE '2024-05-10', 'Broken windshield', 500.00);
INSERT INTO claims (id, car_id, claim_date, description, amount) VALUES (2, 1, DATE '2025-02-20', 'Minor accident', 1500.00);

INSERT INTO insurancepolicy (id, car_id, provider, start_date, end_date) VALUES (99, 1, 'TestInsurer', DATE '2025-01-01', DATE '2025-08-30');

ALTER TABLE claims ALTER COLUMN id RESTART WITH 3;

ALTER TABLE car ALTER COLUMN id RESTART WITH 1000;

