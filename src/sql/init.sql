drop table if exists users cascade;
drop table if exists poems cascade;
drop table if exists likers cascade;

create table users (
  id        bigserial primary key,
  name      varchar(100) not null unique, -- twitter name
  link      text not null -- twitter link
);

insert into users (name, link) values ('poetree', 'http://clojurecup.com/poetree');


create table poems (
  id         bigserial primary key,
  line_order integer not null,
  type       varchar(100) not null,
  content    text not null,
  users_id   integer references users(id),
  poems_id   integer references poems(id)
);

insert into poems (line_order, type, content, users_id, poems_id) 
            values (1, 'HAIKU', 'Little semicolon', 1, null);
insert into poems (line_order, type, content, users_id, poems_id) 
            values (2, 'HAIKU', 'that makes me not compile this,', 1, 1);
insert into poems (line_order, type, content, users_id, poems_id) 
            values (3, 'HAIKU', 'where are you missing?', 1, 2);

create table likers (
  poems_id   integer references poems(id),
  users_id   integer references users(id),
  constraint unique_likes unique (poems_id, users_id)
);

insert into likers (poems_id, users_id) values (3, 1);
