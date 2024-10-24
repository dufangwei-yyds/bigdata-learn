use data_ingestion;

create table if not exists compute_column (
    id int(11) not null,
    name varchar(255),
    age int(11),
    dt datetime(0),
    primary key (id)
);

insert into compute_column values (1, 'John', 10, '2020-01-01 00:00:00');
insert into compute_column values (2, 'Jane', 20, '2020-01-01 00:00:00');
insert into compute_column values (3, 'Jim', 30, '2020-01-01 00:00:00');
