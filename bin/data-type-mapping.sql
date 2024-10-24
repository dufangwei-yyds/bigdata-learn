use data_ingestion;

create table if not exists data_type_mapping (
    id int(11) not null,
    b1 bit(1),
    t1 tinyint(1),
    i1 int(1),
    primary key (id)
);

insert into data_type_mapping values (1, 1, 1, 1);
