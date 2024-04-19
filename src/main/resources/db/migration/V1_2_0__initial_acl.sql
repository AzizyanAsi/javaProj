-- Idonow initial ACL

-- INSERT ROLES

INSERT INTO role
    (role_type, description)
VALUES ('ADMIN', 'Admin permissions'),
       ('SUPPORT_AGENT', 'Support agent permissions'),
       ('CLIENT', 'Client permissions'),
       ('PROFESSIONAL', 'Professional permissions');
