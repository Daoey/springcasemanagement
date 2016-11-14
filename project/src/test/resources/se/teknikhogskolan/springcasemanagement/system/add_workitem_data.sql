insert into Team (id, name, active) values (2465878, 'Teenage Mutant Ninja Turtles', true);

insert into User (id, username, active, usernumber) values (68165, 'Splinter', true, 10001);
insert into User (id, username, active, team_id, usernumber) values (22523, 'Raphael', true, 2465878, 10002);
insert into User (id, username, active, team_id, usernumber) values (26344, 'Leonardo', true, 2465878, 10003);
insert into User (id, username, active, team_id, usernumber) values (25255, 'Donatello', true, 2465878, 10004);
insert into User (id, username, active, team_id, usernumber) values (23523, 'Michaelangelo', true, 2465878, 10005);
insert into User (id, username, active, usernumber) values (6546546, 'Crang', false, 20001);
insert into User (id, username, active, usernumber) values (6165111, 'Shredder', true, 20002);

insert into Issue (id, active, description) values (123541, true, 'The turtles need pizza for energy to fight');

insert into WorkItem (id, created, description, status, user_id) 
	values (98481111, '2016-11-11', 'Be evil', 'UNSTARTED', 6165111);

insert into WorkItem (id, created, description, status, user_id) 
	values (98422222, '2016-11-11', 'Take over world', 'UNSTARTED', 6165111);

insert into WorkItem (id, created, description, status, user_id) 
	values (98481212, '2016-11-11', 'Lead foot clan', 'UNSTARTED', 6165111);

insert into WorkItem (id, created, description, status, user_id) 
	values (98481234, '2016-11-11', 'Make friend with Crang', 'UNSTARTED', 6165111);

insert into WorkItem (id, created, description, status, user_id) 
	values (10186464, '2016-11-11', 'Create evil mutants', 'UNSTARTED', 6165111);

insert into WorkItem (id, created, description, status, user_id) 
	values (98486464, '2016-11-11', 'Lead the team in battle', 'UNSTARTED', 26344);
	
insert into WorkItem (id, created, description, status, user_id) 
	values (45634545, '2016-11-11', 'Keep everybody smiling', 'STARTED', 23523);
	
insert into WorkItem (id, created, description, status) 
	values (8658766, '2016-11-11', 'Invent cool stuff', 'STARTED');
	
insert into WorkItem (id, created, description, status, user_id, issue_id) 
	values (12343456, '2016-11-11', 'Lead TMNT', 'STARTED', 68165, 123541);