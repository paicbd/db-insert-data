DB Insert Data Service
The service db-insert-data, is responsible for inserting data into a PostgreSQL database. It retrieves data from Redis and inserts it into the database based on a predefined schedule. The application is designed to handle large volumes of data efficiently using batching techniques, multithreading, and connection pooling. The service includes support for cluster configurations and database schema migrations using Flyway.

Main Responsibilities:
Fetch data from Redis.
Batch data insertion into a PostgreSQL database.
Support for clustering with multiple Redis nodes.
Manage connection pools and worker threads for efficient processing.
Automated database migrations using Flyway.
Configurable via environment variables for performance tuning.
Key Parameters
1. Environment Variables
JVM Parameters:

JVM_XMS: Initial heap size for the JVM (default: 512MB).
JVM_XMX: Maximum heap size for the JVM (default: 1024MB).
Server Configuration:

SERVER_PORT: Port on which the application runs (default: 8090).
APPLICATION_NAME: Name of the application (default: "db-insert-data-app").
Database Configuration:

DATA_SOURCE_URL: JDBC URL to the PostgreSQL database.
DATA_SOURCE_USER_NAME: Username for the PostgreSQL database.
DATA_SOURCE_PASSWORD: Password for the PostgreSQL database.
DATA_SOURCE_DRIVER_CLASS_NAME: JDBC driver class name (default: PostgreSQL).
DATA_SOURCE_HIKARI_PROPERTIES: Whether HikariCP (connection pooling) properties are enabled.
Redis Cluster Configuration:

CLUSTER_NODES: List of Redis cluster nodes, formatted as host:port (default: 10 nodes from localhost:7000 to localhost:7009).
Thread Pool Configuration:

THREAD_POOL_MAX_TOTAL: Maximum number of threads in the pool (default: 20).
THREAD_POOL_MIN_IDLE: Minimum number of idle threads (default: 5).
THREAD_POOL_MAX_IDLE: Maximum number of idle threads (default: 10).
THREAD_POOL_BLOCK_WHEN_EXHAUSTED: Whether to block when the thread pool is exhausted (default: true).
Data Processing Configuration:

CONFIGURATION_CDR: Redis list name for CDR (Call Detail Records).
CONFIGURATION_CDR_WORKERS: Number of worker threads for processing CDR records (default: 1).
CONFIGURATION_CDR_BATCH_SIZE: Batch size for Redis-to-database inserts (default: 15000).
CONFIGURATION_CDR_RECORDS_TAKE: Number of records to take from Redis (default: 1,000,000).
CONFIGURATION_INTERNAL_MILLIS: Time interval in milliseconds for scheduled tasks (default: 1000ms).
Database Retry Configuration:

JDBC_MAX_RETRIES: Maximum number of retries for database operations (default: 5).
Flyway (Database Migration) Configuration:

FLYWAY_ENABLED: Whether Flyway is enabled for schema migration (default: true).
FLYWAY_TABLE: Flyway history table name (default: _flyway_history).
FLYWAY_CLEAN_DISABLED: Whether Flyway clean is disabled (default: false).
FLYWAY_BASELINE_ON_MIGRATE: Whether to baseline schema on migration (default: true).
FLYWAY_CLEAN_ON_VALIDATE_ERROR: Whether to clean schema on validation error (default: true).
Application Mode:

APPLICATION_MODE: Mode of operation (default: "logs"). Other options are database and kafka.
APPLICATION_CDR_SEPARATOR: Separator used for CDR records (default: "|").
APPLICATION_CDR_LOCATION: Location for storing CDR logs (default: /var/log).
JMX Configuration:

ENABLE_JMX: Whether to enable JMX (default: true).
IP_JMX: IP address for JMX monitoring (default: 127.0.0.1).
JMX_PORT: Port for JMX monitoring (default: 9010).
2. Resource Limits
ulimits: The service sets a high number of allowed open file descriptors:
soft: 1,000,000
hard: 1,000,000
3. Volumes
The service mounts a custom configuration file for logging:
/opt/paic/smsc-docker/insert-data/db-insert-data-docker/resources/conf/logback.xml to /opt/paic/DB_INSERT_DATA/conf/logback.xml.

This db-insert-data service is highly customizable through these environment variables, allowing for efficient data processing and flexible connection setups for both Redis clusters and PostgreSQL databases.