create table RuleOperator(Operator_id int(3) AUTO_INCREMENT, DataType varchar(25), Operators varchar(250), status TINYINT(1), PRIMARY KEY ( Operator_id ));
create table RuleQueryMaster(RuleID int(5) AUTO_INCREMENT, source_id varchar(10), Rule_Queries varchar(1000), source_fields varchar(400), PRIMARY KEY (RuleID));

insert into RuleOperator (DataType,Operators,status) values ('number', '>',1);
insert into RuleOperator (DataType,Operators,status) values ('number', '<',1);
insert into RuleOperator (DataType,Operators,status) values ('number', '=',1);
insert into RuleOperator (DataType,Operators,status) values ('number', '!=',1);
insert into RuleOperator (DataType,Operators,status) values ('string', 'regex',1);
insert into RuleOperator (DataType,Operators,status) values ('string', 'equals',1);
insert into RuleOperator (DataType,Operators,status) values ('location', 'coordinates',1);
insert into RuleOperator (DataType,Operators,status) values ('location', 'address',1);
insert into RuleOperator (DataType,Operators,status) values ('location', 'radius',1);
insert into RuleOperator (DataType,Operators,status) values ('number', 'not',0);
insert into RuleOperator (DataType,Operators,status) values ('number', 'between',0);
insert into RuleOperator (DataType,Operators,status) values ('number', 'outOfRange',0);
insert into RuleOperator (DataType,Operators,status) values ('string', 'in', 0);
insert into RuleOperator (DataType,Operators,status) values ('string', 'equalsIgnoreCase', 0);