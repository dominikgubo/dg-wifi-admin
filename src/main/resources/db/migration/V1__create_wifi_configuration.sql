create table wifi_configuration (
    cpe_id text not null primary key,
    wifi_band varchar(32) not null,
    ssid text not null,
    encryption_type varchar(32) not null,
    password text,
    last_synced_at timestamp with time zone not null
);
