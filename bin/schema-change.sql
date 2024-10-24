use data_ingestion;

create table if not exists schema_change (
    f1 int(11) not null,
    f2 int(11),
    f3 int(11),
    primary key (f1)
);

insert into schema_change values (1, 1, 1);
insert into schema_change values (2, 2, 2);
insert into schema_change values (3, 3, 3);

alter table schema_change add column f4 int(11);

insert into schema_change values (4, 4, 4, 4);
