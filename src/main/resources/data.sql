-- Limpar tabelas
DELETE FROM users;

-- Inserir usuário admin
INSERT INTO users (name, email, password, role) VALUES (
    'Admin',
    'admin@example.com',
    '$2a$10$XptfskLsT1l/bRTLRiiCgejHqOpgXFreUnNUaBMgBDAueaoXK3S2K', -- senha: admin123
    'ADMIN'
);

-- Inserir usuário comum
INSERT INTO users (name, email, password, role) VALUES (
    'Usuário Teste',
    'usuario@example.com',
    '$2a$10$XptfskLsT1l/bRTLRiiCgejHqOpgXFreUnNUaBMgBDAueaoXK3S2K', -- senha: admin123
    'USER'
);
