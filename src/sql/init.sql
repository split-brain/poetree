drop table if exists users cascade;
drop table if exists poems cascade;
drop table if exists likers cascade;

     create table users (
  id        bigserial primary key,
  name      varchar(100) not null unique, -- twitter name
  profile_image_url text null,
  access_token text, -- should be not null, but leave as is for now
  access_token_secret text
);

insert into users (name) values ('poetree');


create table poems (
  id         bigserial primary key,
  line_order integer not null,
  type       varchar(100) not null,
  content    text not null,
  lang       text not null,
  ts         TIMESTAMP not null DEFAULT CURRENT_TIMESTAMP,
  users_id   integer references users(id),
  poems_id   integer references poems(id)
);

insert into poems (line_order, type, content, lang, users_id, poems_id)
values (1, 'HAIKU', 'Little semicolon', 'EN', 1, null);
insert into poems (line_order, type, content, lang, users_id, poems_id)
values (2, 'HAIKU', 'that makes me not compile this', 'EN', 1, 1);
insert into poems (line_order, type, content, lang, users_id, poems_id)
values (3, 'HAIKU', 'where are you missing?','EN', 1, 2);

insert into poems (line_order, type, content, lang, users_id, poems_id)
values (1, 'HAIKU', 'Start in the middle', 'EN', 1, null);
insert into poems (line_order, type, content, lang, users_id, poems_id)
values (2, 'HAIKU', 'Then go halfway up or down', 'EN', 1, 1);
insert into poems (line_order, type, content, lang, users_id, poems_id)
values (3, 'HAIKU', 'Repeat until done','EN', 1, 2);

insert into poems (line_order, type, content, lang, users_id, poems_id)
values (1, 'HAIKU', 'World is vast and wide', 'EN', 1, null);
insert into poems (line_order, type, content, lang, users_id, poems_id)
values (2, 'HAIKU', 'So much out there to explore', 'EN', 1, 1);
insert into poems (line_order, type, content, lang, users_id, poems_id)
values (3, 'HAIKU', 'Right now, let''s eat lunch','EN', 1, 2);

create table likers (
  poems_id   integer references poems(id),
  users_id   integer references users(id),
  constraint unique_likes unique (poems_id, users_id)
);

insert into likers (poems_id, users_id) values (3, 1);
