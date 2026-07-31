CREATE TABLE IF NOT EXISTS cdr_status_code
(
    status_code             VARCHAR(50),
    status                  VARCHAR(50),
    protocol                VARCHAR(50),
    PRIMARY KEY (status_code, protocol)
);

INSERT INTO cdr_status_code (status_code, status, protocol)
VALUES ('98', 'EXPIRED', 'SMPP')
    ON CONFLICT (status_code, protocol) DO NOTHING;

INSERT INTO cdr_status_code (status_code, status, protocol) VALUES
                                                                ('4', 'UNDELIVERED', 'SMPP'),
                                                                ('5', 'UNDELIVERED', 'SMPP'),
                                                                ('20', 'UNDELIVERED', 'SMPP'),
                                                                ('88', 'UNDELIVERED', 'SMPP'),
                                                                ('99', 'UNDELIVERED', 'SMPP'),
                                                                ('195', 'UNDELIVERED', 'SMPP'),
                                                                ('254', 'UNDELIVERED', 'SMPP'),
                                                                ('255', 'UNDELIVERED', 'SMPP'),
                                                                ('258', 'UNDELIVERED', 'SMPP'),
                                                                ('300', 'UNDELIVERED', 'SMPP'),
                                                                ('504', 'UNDELIVERED', 'SMPP'),
                                                                ('506', 'UNDELIVERED', 'SMPP')
    ON CONFLICT (status_code, protocol) DO NOTHING;


INSERT INTO cdr_status_code (status_code, status, protocol) VALUES
                                                                ('1', 'REJECTED', 'SMPP'),
                                                                ('2', 'REJECTED', 'SMPP'),
                                                                ('3', 'REJECTED', 'SMPP'),
                                                                ('6', 'REJECTED', 'SMPP'),
                                                                ('7', 'REJECTED', 'SMPP'),
                                                                ('8', 'REJECTED', 'SMPP'),
                                                                ('10', 'REJECTED', 'SMPP'),
                                                                ('11', 'REJECTED', 'SMPP'),
                                                                ('12', 'REJECTED', 'SMPP'),
                                                                ('13', 'REJECTED', 'SMPP'),
                                                                ('14', 'REJECTED', 'SMPP'),
                                                                ('15', 'REJECTED', 'SMPP'),
                                                                ('17', 'REJECTED', 'SMPP'),
                                                                ('19', 'REJECTED', 'SMPP'),
                                                                ('21', 'REJECTED', 'SMPP'),
                                                                ('51', 'REJECTED', 'SMPP'),
                                                                ('52', 'REJECTED', 'SMPP'),
                                                                ('64', 'REJECTED', 'SMPP'),
                                                                ('66', 'REJECTED', 'SMPP'),
                                                                ('67', 'REJECTED', 'SMPP'),
                                                                ('69', 'REJECTED', 'SMPP'),
                                                                ('72', 'REJECTED', 'SMPP'),
                                                                ('73', 'REJECTED', 'SMPP'),
                                                                ('80', 'REJECTED', 'SMPP'),
                                                                ('81', 'REJECTED', 'SMPP'),
                                                                ('83', 'REJECTED', 'SMPP'),
                                                                ('84', 'REJECTED', 'SMPP'),
                                                                ('85', 'REJECTED', 'SMPP'),
                                                                ('97', 'REJECTED', 'SMPP'),
                                                                ('100', 'REJECTED', 'SMPP'),
                                                                ('101', 'REJECTED', 'SMPP'),
                                                                ('102', 'REJECTED', 'SMPP'),
                                                                ('103', 'REJECTED', 'SMPP'),
                                                                ('192', 'REJECTED', 'SMPP'),
                                                                ('193', 'REJECTED', 'SMPP'),
                                                                ('194', 'REJECTED', 'SMPP'),
                                                                ('196', 'REJECTED', 'SMPP'),
                                                                ('256', 'REJECTED', 'SMPP'),
                                                                ('257', 'REJECTED', 'SMPP'),
                                                                ('259', 'REJECTED', 'SMPP'),
                                                                ('260', 'REJECTED', 'SMPP'),
                                                                ('261', 'REJECTED', 'SMPP'),
                                                                ('262', 'REJECTED', 'SMPP'),
                                                                ('263', 'REJECTED', 'SMPP'),
                                                                ('264', 'REJECTED', 'SMPP'),
                                                                ('265', 'REJECTED', 'SMPP'),
                                                                ('266', 'REJECTED', 'SMPP'),
                                                                ('267', 'REJECTED', 'SMPP'),
                                                                ('268', 'REJECTED', 'SMPP'),
                                                                ('269', 'REJECTED', 'SMPP'),
                                                                ('270', 'REJECTED', 'SMPP'),
                                                                ('271', 'REJECTED', 'SMPP'),
                                                                ('272', 'REJECTED', 'SMPP'),
                                                                ('273', 'REJECTED', 'SMPP'),
                                                                ('274', 'REJECTED', 'SMPP'),
                                                                ('500', 'REJECTED', 'SMPP'),
                                                                ('505', 'REJECTED', 'SMPP')
    ON CONFLICT (status_code, protocol) DO NOTHING;


INSERT INTO cdr_status_code (status_code, status, protocol) VALUES
                                                                ('10', 'REJECTED', 'SS7'),
                                                                ('11', 'REJECTED', 'SS7'),
                                                                ('17', 'REJECTED', 'SS7'),
                                                                ('23', 'REJECTED', 'SS7'),
                                                                ('47', 'REJECTED', 'SS7'),
                                                                ('48', 'REJECTED', 'SS7'),
                                                                ('49', 'REJECTED', 'SS7'),
                                                                ('52', 'REJECTED', 'SS7'),
                                                                ('53', 'REJECTED', 'SS7'),
                                                                ('60', 'REJECTED', 'SS7'),
                                                                ('61', 'REJECTED', 'SS7'),
                                                                ('500', 'REJECTED', 'SS7'),
                                                                ('508', 'REJECTED', 'SS7')
    ON CONFLICT (status_code, protocol) DO NOTHING;


INSERT INTO cdr_status_code (status_code, status, protocol) VALUES
                                                                ('1', 'UNDELIVERED', 'SS7'),
                                                                ('2', 'UNDELIVERED', 'SS7'),
                                                                ('3', 'UNDELIVERED', 'SS7'),
                                                                ('5', 'UNDELIVERED', 'SS7'),
                                                                ('6', 'UNDELIVERED', 'SS7'),
                                                                ('7', 'UNDELIVERED', 'SS7'),
                                                                ('8', 'UNDELIVERED', 'SS7'),
                                                                ('9', 'UNDELIVERED', 'SS7'),
                                                                ('12', 'UNDELIVERED', 'SS7'),
                                                                ('13', 'UNDELIVERED', 'SS7'),
                                                                ('14', 'UNDELIVERED', 'SS7'),
                                                                ('15', 'UNDELIVERED', 'SS7'),
                                                                ('16', 'UNDELIVERED', 'SS7'),
                                                                ('18', 'UNDELIVERED', 'SS7'),
                                                                ('19', 'UNDELIVERED', 'SS7'),
                                                                ('20', 'UNDELIVERED', 'SS7'),
                                                                ('21', 'UNDELIVERED', 'SS7'),
                                                                ('22', 'UNDELIVERED', 'SS7'),
                                                                ('24', 'UNDELIVERED', 'SS7'),
                                                                ('25', 'UNDELIVERED', 'SS7'),
                                                                ('26', 'UNDELIVERED', 'SS7'),
                                                                ('27', 'UNDELIVERED', 'SS7'),
                                                                ('28', 'UNDELIVERED', 'SS7'),
                                                                ('29', 'UNDELIVERED', 'SS7'),
                                                                ('30', 'UNDELIVERED', 'SS7'),
                                                                ('31', 'UNDELIVERED', 'SS7'),
                                                                ('32', 'UNDELIVERED', 'SS7'),
                                                                ('33', 'UNDELIVERED', 'SS7'),
                                                                ('34', 'UNDELIVERED', 'SS7'),
                                                                ('35', 'UNDELIVERED', 'SS7'),
                                                                ('36', 'UNDELIVERED', 'SS7'),
                                                                ('37', 'UNDELIVERED', 'SS7'),
                                                                ('38', 'UNDELIVERED', 'SS7'),
                                                                ('39', 'UNDELIVERED', 'SS7'),
                                                                ('40', 'UNDELIVERED', 'SS7'),
                                                                ('42', 'UNDELIVERED', 'SS7'),
                                                                ('43', 'UNDELIVERED', 'SS7'),
                                                                ('44', 'UNDELIVERED', 'SS7'),
                                                                ('45', 'UNDELIVERED', 'SS7'),
                                                                ('46', 'UNDELIVERED', 'SS7'),
                                                                ('50', 'UNDELIVERED', 'SS7'),
                                                                ('51', 'UNDELIVERED', 'SS7'),
                                                                ('54', 'UNDELIVERED', 'SS7'),
                                                                ('58', 'UNDELIVERED', 'SS7'),
                                                                ('59', 'UNDELIVERED', 'SS7'),
                                                                ('62', 'UNDELIVERED', 'SS7'),
                                                                ('71', 'UNDELIVERED', 'SS7'),
                                                                ('72', 'UNDELIVERED', 'SS7'),
                                                                ('300', 'UNDELIVERED', 'SS7'),
                                                                ('507', 'UNDELIVERED', 'SS7'),
                                                                ('509', 'UNDELIVERED', 'SS7'),
                                                                ('510', 'UNDELIVERED', 'SS7'),
                                                                ('511', 'UNDELIVERED', 'SS7')
    ON CONFLICT (status_code, protocol) DO NOTHING;


INSERT INTO cdr_status_code (status_code, status, protocol) VALUES
                                                                ('401', 'REJECTED', 'HTTP'),
                                                                ('403', 'REJECTED', 'HTTP'),
                                                                ('405', 'REJECTED', 'HTTP'),
                                                                ('409', 'REJECTED', 'HTTP'),
                                                                ('415', 'REJECTED', 'HTTP'),
                                                                ('500', 'REJECTED', 'HTTP')
    ON CONFLICT (status_code, protocol) DO NOTHING;


INSERT INTO cdr_status_code (status_code, status, protocol) VALUES
                                                                ('400', 'UNDELIVERED', 'HTTP'),
                                                                ('404', 'UNDELIVERED', 'HTTP'),
                                                                ('408', 'UNDELIVERED', 'HTTP'),
                                                                ('410', 'UNDELIVERED', 'HTTP'),
                                                                ('429', 'UNDELIVERED', 'HTTP'),
                                                                ('501', 'UNDELIVERED', 'HTTP'),
                                                                ('502', 'UNDELIVERED', 'HTTP'),
                                                                ('503', 'UNDELIVERED', 'HTTP'),
                                                                ('504', 'UNDELIVERED', 'HTTP')
    ON CONFLICT (status_code, protocol) DO NOTHING;
