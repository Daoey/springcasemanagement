insert into Team (id, name, active) values (2465878, 'Teenage Mutant Ninja Turtles', true);

insert into User (id, username, active, team_id, usernumber) values (22523, 'Raphael', true, 2465878, 124234);
insert into User (id, username, active, team_id, usernumber) values (26344, 'Leonardo', true, 2465878, 124224);
insert into User (id, username, active, team_id, usernumber) values (25255, 'Donatello', true, 2465878, 924234);
insert into User (id, username, active, team_id, usernumber) values (23523, 'Michaelangelo', true, 2465878, 184234);
insert into User (id, username, active) values (10001, 'Splinter', true);

insert into Issue (id, active, description) values (123541, true, 'The turtles need pizza for energy to fight');

insert into WorkItem (id, created, description, status, user_id) 
	values (98486464, '2016-11-11', 'Lead the team in battle', 'UNSTARTED', 26344);
	
insert into WorkItem (id, created, description, status, user_id) 
	values (45634545, '2016-11-11', 'Keep everybody smiling', 'STARTED', 23523);
	
insert into WorkItem (id, created, description, status, user_id, issue_id) 
	values (12343456, '2016-11-11', 'Lead TMNT', 'STARTED', 10001, 123541);