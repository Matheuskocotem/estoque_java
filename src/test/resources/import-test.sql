-- Limpar tabelas antes dos testes
DELETE FROM user_roles;
DELETE FROM users;

-- Inserir usuário de teste
INSERT INTO users (id, name, email, password, role, created_at, updated_at)
VALUES (1, 'Test User', 'test@example.com', '$2a$10$XptfskLsT1SL/bOzZLzJle1oG88ogJgwDsCLionp7qTyOwgJdwXrG', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Inserir papéis
INSERT INTO user_roles (user_id, role) VALUES (1, 'USER');
