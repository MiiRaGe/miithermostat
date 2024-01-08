package miithermostat.models

import miithermostat.getDb

fun createTables() {
    val db = getDb()

    db.useConnection { conn ->

    val createExtension = """
    --
    -- Add timescales extensions
    --
    
    --  Make sure this is ran by whomever created the DB
    -- create extension IF NOT EXISTS timescaledb_toolkit; TODO: Find a way to compile on RPi5?
    create extension IF NOT EXISTS timescaledb;
    """
    conn.prepareStatement(createExtension).execute()

    val deviceSql = """
    --
    -- Name: device; Type: TABLE; Schema: public; Owner: postgres
    --
    
    CREATE TABLE IF NOT EXISTS device (
        id text primary key NOT NULL,
        name text
    );
    """
    conn.prepareStatement(deviceSql).execute()

    val locationSql =
    """
    --
    -- Name: Available Location (Like rooms)
    --
    
    CREATE TABLE IF NOT EXISTS location (
        name text primary key NOT NULL
    );
    """
    conn.prepareStatement(locationSql).execute()

    val deviceLocationSql =
    """
    --
    -- Name: Many-to-One Device -> Location
    --
    
    CREATE TABLE IF NOT EXISTS device_location (
        device_id text primary key NOT NULL,
        location text NOT NULL,
        constraint fk_device foreign key(device_id) references device(id) on update cascade,
        constraint fk_location foreign key(location) references location(name) on update cascade
    );
    """
    conn.prepareStatement(deviceLocationSql).execute()

    val sensorDataSql = """
    --
    -- Name: Table the sensor data will be stored.
    --
    
    CREATE TABLE IF NOT EXISTS sensor_data (
        "time" timestamp with time zone DEFAULT now() NOT NULL,
        device_id text NOT NULL,
        temperature_mc smallint NOT NULL,
        humidity_pt smallint NOT NULL,
        location text NOT NULL,
        constraint fk_device foreign key(device_id) references device(id) on update cascade,
        constraint fk_location foreign key(location) references location(name) on update cascade
    );

    --
    -- Add timescaledb on the sensor_data
    --
    select create_hypertable('sensor_data', 'time', if_not_exists => true, migrate_data => true);
    """
    
    conn.prepareStatement(sensorDataSql).execute()

    val sensorDataWithOffsetSql ="""
    --
    -- Name: Table the adjusted sensor data will be stored.
    --
    
    CREATE TABLE IF NOT EXISTS sensor_data_with_offset (
        "time" timestamp with time zone DEFAULT now() NOT NULL,
        device_id text NOT NULL,
        temperature_mc smallint NOT NULL,
        humidity_pt smallint NOT NULL,
        location text NOT NULL,
        constraint fk_device foreign key(device_id) references device(id) on update cascade,
        constraint fk_location foreign key(location) references location(name) on update cascade
    );

    --
    -- Add timescaledb on the sensor_data
    --
    select create_hypertable('sensor_data_with_offset', 'time', if_not_exists => true, migrate_data => true);
    """
    
    conn.prepareStatement(sensorDataWithOffsetSql).execute()

    val devicesOffsetSql = 
    """
    --
    -- Name: Table the offset per device will be stored.
    --
    
    CREATE TABLE IF NOT EXISTS devices_offset (
        device_id text primary key NOT NULL,
        temperature_mc_offset smallint NOT NULL,
        humidity_pt_offset smallint NOT NULL,
        constraint fk_device foreign key(device_id) references device(id) on update cascade
    );
    """
    
    conn.prepareStatement(devicesOffsetSql).execute()
 }
}
