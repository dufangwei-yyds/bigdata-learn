use data_ingestion;

create table if not exists chinese_code (
    id int(11) not null,
    name varchar(255),
    primary key (id)
);

insert into chinese_code values (1, '张三');
insert into chinese_code values (2, '李四');
