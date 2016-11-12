INSERT INTO Team (id, created, active, name) VALUES (1, '2016-11-11', true, 'Light side');
INSERT INTO Team (id, created, active, name) VALUES (2, '2016-11-11', true, 'Dark side');

INSERT INTO User (id, userNumber, created, active, username, firstName, lastName, team_id) VALUES (10, 1, '2016-11-11', true, 'Robotarm Luke', 'Luke', 'Skywalker', 1);
INSERT INTO User (id, userNumber, created, active, username, firstName, lastName, team_id) VALUES (11, 2, '2016-11-11', true, 'I am your father', 'Darth', 'Vader', 2);
INSERT INTO User (id, userNumber, created, active, username, firstName, lastName, team_id) VALUES (12, 3, '2016-11-13', true, 'I am your sister', 'Leia', 'Skywalker', 1);
INSERT INTO User (id, userNumber, created, active, username, firstName, lastName, team_id) VALUES (13, 4, '2016-11-13', true, 'Master Yoda', 'Yoda', '', 1);

INSERT INTO WorkItem (id, created, description, user_id) VALUES (1, '2016-11-11', 'Destroy deathstar', 10);
INSERT INTO WorkItem (id, created, description, user_id) VALUES (2, '2016-11-11', 'Train to be a yedi', 10);
