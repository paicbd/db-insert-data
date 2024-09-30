CREATE TABLE cdr
(
    id                        SERIAL PRIMARY KEY,
    record_date               BIGINT,
    submit_date               BIGINT,
    delivery_date             BIGINT,
    message_type              VARCHAR(10),
    message_id                VARCHAR(50),
    origination_protocol      VARCHAR(20),
    origination_network_id    VARCHAR(20),
    origination_type          VARCHAR(5),
    destination_type          VARCHAR(20),
    destination_protocol      VARCHAR(20),
    destination_network_id    VARCHAR(20),
    routing_id                INT,
    status                    VARCHAR(50),
    status_code               VARCHAR(50),
    comment                   VARCHAR(50),
    dialog_duration           BIGINT,
    processing_time           BIGINT,
    data_coding               INT,
    validity_period           VARCHAR(50),
    addr_src_digits           VARCHAR(50),
    addr_src_ton              INT,
    addr_src_npi              INT,
    addr_dst_digits           VARCHAR(50),
    addr_dst_ton              INT,
    addr_dst_npi              INT,
    remote_dialog_id          BIGINT,
    local_dialog_id           BIGINT,
    local_spc                 INT,
    local_ssn                 INT,
    local_global_title_digits VARCHAR(255),
    remote_spc                INT,
    remote_ssn                INT,
    remote_global_title_digits VARCHAR(50),
    imsi                      VARCHAR(50),
    nnn_digits                VARCHAR(50),
    originator_sccp_address   VARCHAR(50),
    mt_service_center_address VARCHAR(50),
    first_20_character_of_sms VARCHAR(20),
    first_20_characters_of_sms VARCHAR(20),
    esm_class                   VARCHAR(20),
    udhi                        VARCHAR(20),
    registered_delivery         INT,
    msg_reference_number        VARCHAR(20),
    total_segment               INT,
    segment_sequence            INT,
    retry_number                INT,
    parent_id                   VARCHAR(50)
);