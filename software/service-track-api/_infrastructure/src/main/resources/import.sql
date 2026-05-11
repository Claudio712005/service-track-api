INSERT INTO usuarios (id, nome, email, senha_hash, data_criacao, data_atualizacao, data_nascimento, telefone, cpf, ativo)
VALUES
('550e8400-e29b-41d4-a716-446655440001', 'João Silva', 'joao.silva@example.com', '$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy', NOW(), NOW(), '1990-05-15', '11987654321', '12345678901', true),
('550e8400-e29b-41d4-a716-446655440002', 'Maria Santos', 'maria.santos@example.com', '$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy', NOW(), NOW(), '1985-03-20', '11987654322', '12345678902', true),
('550e8400-e29b-41d4-a716-446655440003', 'Pedro Oliveira', 'pedro.oliveira@example.com', '$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy', NOW(), NOW(), '1992-07-08', '11987654323', '12345678903', true),
('550e8400-e29b-41d4-a716-446655440004', 'Ana Costa', 'ana.costa@example.com', '$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy', NOW(), NOW(), '1988-09-12', '11987654324', '12345678904', true),
('550e8400-e29b-41d4-a716-446655440005', 'Lucas Ferreira', 'lucas.ferreira@example.com', '$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy', NOW(), NOW(), '1995-11-25', '11987654325', '12345678905', true),
('550e8400-e29b-41d4-a716-446655440006', 'Fernanda Gomes', 'fernanda.gomes@example.com', '$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy', NOW(), NOW(), '1991-04-30', '11987654326', '12345678906', true),
('550e8400-e29b-41d4-a716-446655440007', 'Carlos Alberto', 'carlos.alberto@example.com', '$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy', NOW(), NOW(), '1986-01-18', '11987654327', '12345678907', true),
('550e8400-e29b-41d4-a716-446655440008', 'Juliana Martins', 'juliana.martins@example.com', '$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy', NOW(), NOW(), '1993-06-22', '11987654328', '12345678908', true),
('550e8400-e29b-41d4-a716-446655440009', 'Roberto Pereira', 'roberto.pereira@example.com', '$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy', NOW(), NOW(), '1980-10-05', '11987654329', '12345678909', true),
('550e8400-e29b-41d4-a716-446655440010', 'Camila Souza', 'camila.souza@example.com', '$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy', NOW(), NOW(), '1994-02-14', '11987654330', '12345678910', true),
('550e8400-e29b-41d4-a716-446655440101', 'Mário Máquina', 'mario.maquina@example.com', '$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy', NOW(), NOW(), '1975-08-10', '11999999001', '98765432101', true),
('550e8400-e29b-41d4-a716-446655440102', 'Técnico João', 'tecnico.joao@example.com', '$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy', NOW(), NOW(), '1982-12-03', '11999999002', '98765432102', true),
('550e8400-e29b-41d4-a716-446655440103', 'Especialista Diego', 'diego.especialista@example.com', '$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy', NOW(), NOW(), '1979-03-15', '11999999003', '98765432103', true),
('550e8400-e29b-41d4-a716-446655440104', 'Mestre Rafael', 'rafael.mestre@example.com', '$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy', NOW(), NOW(), '1977-07-22', '11999999004', '98765432104', true),
('550e8400-e29b-41d4-a716-446655440105', 'Profissional Anderson', 'anderson.prof@example.com', '$2a$10$QjHYDvLyL5p5xWxBm1VYWe/NnLCFNVdF.VbiP5U7kRfXQ4pHZRziy', NOW(), NOW(), '1984-11-08', '11999999005', '98765432105', true)
ON CONFLICT DO NOTHING;

INSERT INTO usuario_roles (usuario_id, role)
VALUES
('550e8400-e29b-41d4-a716-446655440001', 'CLIENTE'),
('550e8400-e29b-41d4-a716-446655440002', 'CLIENTE'),
('550e8400-e29b-41d4-a716-446655440003', 'CLIENTE'),
('550e8400-e29b-41d4-a716-446655440004', 'CLIENTE'),
('550e8400-e29b-41d4-a716-446655440005', 'CLIENTE'),
('550e8400-e29b-41d4-a716-446655440006', 'CLIENTE'),
('550e8400-e29b-41d4-a716-446655440007', 'CLIENTE'),
('550e8400-e29b-41d4-a716-446655440008', 'CLIENTE'),
('550e8400-e29b-41d4-a716-446655440009', 'CLIENTE'),
('550e8400-e29b-41d4-a716-446655440010', 'CLIENTE'),
('550e8400-e29b-41d4-a716-446655440101', 'MECANICO'),
('550e8400-e29b-41d4-a716-446655440102', 'MECANICO'),
('550e8400-e29b-41d4-a716-446655440103', 'MECANICO'),
('550e8400-e29b-41d4-a716-446655440104', 'MECANICO'),
('550e8400-e29b-41d4-a716-446655440105', 'MECANICO')
ON CONFLICT DO NOTHING;

INSERT INTO veiculos (veiculo_id, placa, modelo, marca, ano, imagem_url, codigo_fipe, ativo, data_criacao, data_atualizacao, proprietario_id)
VALUES
('660e8400-e29b-41d4-a716-446655550001', 'ABC1234', 'Civic', 'Honda', 2020, 'https://images.unsplash.com/photo-1464219414232-fdb7452e77ca?w=400&h=300&fit=crop', NULL, 'S', NOW(), NOW(), '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655550002', 'XYZ9876', 'Gol', 'Volkswagen', 2019, 'https://images.unsplash.com/photo-1562162192-3c90c1c1b1fa?w=400&h=300&fit=crop', NULL, 'S', NOW(), NOW(), '550e8400-e29b-41d4-a716-446655440002'),
('660e8400-e29b-41d4-a716-446655550003', 'DEF5678', 'Corolla', 'Toyota', 2021, 'https://images.unsplash.com/photo-1605559424843-9e4c3ca4b7f1?w=400&h=300&fit=crop', NULL, 'S', NOW(), NOW(), '550e8400-e29b-41d4-a716-446655440003'),
('660e8400-e29b-41d4-a716-446655550004', 'GHI3456', 'Onix', 'Chevrolet', 2022, 'https://images.unsplash.com/photo-1568605114967-8130f3a36994?w=400&h=300&fit=crop', NULL, 'S', NOW(), NOW(), '550e8400-e29b-41d4-a716-446655440004'),
('660e8400-e29b-41d4-a716-446655550005', 'JKL1111', 'HB20', 'Hyundai', 2021, 'https://images.unsplash.com/photo-1542282088-fe8426682b8f?w=400&h=300&fit=crop', NULL, 'S', NOW(), NOW(), '550e8400-e29b-41d4-a716-446655440005'),
('660e8400-e29b-41d4-a716-446655550006', 'MNO2222', 'Tracker', 'Chevrolet', 2023, 'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=400&h=300&fit=crop', NULL, 'S', NOW(), NOW(), '550e8400-e29b-41d4-a716-446655440006'),
('660e8400-e29b-41d4-a716-446655550007', 'PQR3333', 'Creta', 'Hyundai', 2020, 'https://images.unsplash.com/photo-1533473359331-35f3a2f3a919?w=400&h=300&fit=crop', NULL, 'S', NOW(), NOW(), '550e8400-e29b-41d4-a716-446655440007'),
('660e8400-e29b-41d4-a716-446655550008', 'STU4444', 'Fit', 'Honda', 2022, 'https://images.unsplash.com/photo-1552820728-8ac41f1ce891?w=400&h=300&fit=crop', NULL, 'S', NOW(), NOW(), '550e8400-e29b-41d4-a716-446655440008'),
('660e8400-e29b-41d4-a716-446655550009', 'VWX5555', 'Fusca', 'Volkswagen', 2009, 'https://images.unsplash.com/photo-1506399773649-6e0ee4a59b94?w=400&h=300&fit=crop', NULL, 'S', NOW(), NOW(), '550e8400-e29b-41d4-a716-446655440009'),
('660e8400-e29b-41d4-a716-446655550010', 'YZA6666', 'Gol', 'Volkswagen', 2018, 'https://images.unsplash.com/photo-1569523139394-de4798aa62b2?w=400&h=300&fit=crop', NULL, 'S', NOW(), NOW(), '550e8400-e29b-41d4-a716-446655440010'),
('660e8400-e29b-41d4-a716-446655550011', 'BCD7777', 'Sandero', 'Renault', 2020, 'https://images.unsplash.com/photo-1605559424843-9e4c3ca4b7f1?w=400&h=300&fit=crop', NULL, 'S', NOW(), NOW(), '550e8400-e29b-41d4-a716-446655440001'),
('660e8400-e29b-41d4-a716-446655550012', 'EFG8888', 'Hilux', 'Toyota', 2019, 'https://images.unsplash.com/photo-1552053831-71594a27c62d?w=400&h=300&fit=crop', NULL, 'S', NOW(), NOW(), '550e8400-e29b-41d4-a716-446655440002'),
('660e8400-e29b-41d4-a716-446655550013', 'HIJ9999', 'Tiggo 5X', 'Chery', 2021, 'https://images.unsplash.com/photo-1552820728-8ac41f1ce891?w=400&h=300&fit=crop', NULL, 'S', NOW(), NOW(), '550e8400-e29b-41d4-a716-446655440003'),
('660e8400-e29b-41d4-a716-446655550014', 'KLM0000', 'S10', 'Chevrolet', 2022, 'https://images.unsplash.com/photo-1506399773649-6e0ee4a59b94?w=400&h=300&fit=crop', NULL, 'S', NOW(), NOW(), '550e8400-e29b-41d4-a716-446655440004'),
('660e8400-e29b-41d4-a716-446655550015', 'NOP1111', 'Argo', 'Fiat', 2020, 'https://images.unsplash.com/photo-1533473359331-35f3a2f3a919?w=400&h=300&fit=crop', NULL, 'S', NOW(), NOW(), '550e8400-e29b-41d4-a716-446655440005')
ON CONFLICT DO NOTHING;

INSERT INTO servicos (id, nome_servico, descricao_servico, valor_referencia, data_criacao, data_atualizacao, ativo)
VALUES
('770e8400-e29b-41d4-a716-446655660001', 'Troca de Óleo', 'Troca completa de óleo do motor com filtro novo', 150.00, NOW(), NOW(), true),
('770e8400-e29b-41d4-a716-446655660002', 'Alinhamento', 'Alinhamento completo do veículo', 200.00, NOW(), NOW(), true),
('770e8400-e29b-41d4-a716-446655660003', 'Balanceamento', 'Balanceamento de pneus', 120.00, NOW(), NOW(), true),
('770e8400-e29b-41d4-a716-446655660004', 'Manutenção de Freios', 'Revisão e manutenção do sistema de freios', 300.00, NOW(), NOW(), true),
('770e8400-e29b-41d4-a716-446655660005', 'Troca de Pneus', 'Troca de todos os pneus do veículo', 400.00, NOW(), NOW(), true),
('770e8400-e29b-41d4-a716-446655660006', 'Diagnóstico Eletrônico', 'Diagnóstico completo do sistema eletrônico', 250.00, NOW(), NOW(), true),
('770e8400-e29b-41d4-a716-446655660007', 'Limpeza do Motor', 'Limpeza interna e externa do motor', 180.00, NOW(), NOW(), true),
('770e8400-e29b-41d4-a716-446655660008', 'Inspeção de Segurança', 'Inspeção completa de segurança do veículo', 220.00, NOW(), NOW(), true),
('770e8400-e29b-41d4-a716-446655660009', 'Polimento e Enceração', 'Polimento e enceração da pintura', 350.00, NOW(), NOW(), true),
('770e8400-e29b-41d4-a716-446655660010', 'Manutenção de Ar Condicionado', 'Revisão e limpeza do sistema de ar condicionado', 280.00, NOW(), NOW(), true)
ON CONFLICT DO NOTHING;

INSERT INTO insumos (id, nome, descricao, custo, estoque_minimo, qtd_estoque, data_criacao, data_atualizacao, ativo)
VALUES
('880e8400-e29b-41d4-a716-446655770001', 'Óleo Motor 5W-30', 'Óleo sintético para motor 5W-30', 45.00, 10, 50, NOW(), NOW(), true),
('880e8400-e29b-41d4-a716-446655770002', 'Filtro de Óleo', 'Filtro de óleo original', 35.00, 5, 40, NOW(), NOW(), true),
('880e8400-e29b-41d4-a716-446655770003', 'Pastilha de Freio', 'Pastilha de freio semi-metálica', 120.00, 3, 20, NOW(), NOW(), true),
('880e8400-e29b-41d4-a716-446655770004', 'Pneu Aro 15', 'Pneu aro 15 para veículos populares', 350.00, 2, 15, NOW(), NOW(), true),
('880e8400-e29b-41d4-a716-446655770005', 'Coroa e Pinhão', 'Kit coroa e pinhão para diferencial', 800.00, 1, 8, NOW(), NOW(), true),
('880e8400-e29b-41d4-a716-446655770006', 'Jogo de Velas', 'Jogo de velas de ignição', 80.00, 5, 25, NOW(), NOW(), true),
('880e8400-e29b-41d4-a716-446655770007', 'Bateria 60Ah', 'Bateria automotiva 60Ah', 450.00, 2, 10, NOW(), NOW(), true),
('880e8400-e29b-41d4-a716-446655770008', 'Filtro de Ar', 'Filtro de ar do motor', 60.00, 5, 30, NOW(), NOW(), true),
('880e8400-e29b-41d4-a716-446655770009', 'Fluido de Freio', 'Fluido de freio DOT4', 45.00, 5, 20, NOW(), NOW(), true),
('880e8400-e29b-41d4-a716-446655770010', 'Líquido Arrefecimento', 'Líquido de arrefecimento concentrado', 35.00, 5, 25, NOW(), NOW(), true),
('880e8400-e29b-41d4-a716-446655770011', 'Corrente de Distribuição', 'Corrente de distribuição para motor', 250.00, 1, 5, NOW(), NOW(), true),
('880e8400-e29b-41d4-a716-446655770012', 'Jogo de Cabos de Vela', 'Jogo de cabos de ignição', 120.00, 2, 12, NOW(), NOW(), true)
ON CONFLICT DO NOTHING;

INSERT INTO mecanicos (usuario_id, valor_hora, nivel)
VALUES
('550e8400-e29b-41d4-a716-446655440101', 85.00, 'SENIOR'),
('550e8400-e29b-41d4-a716-446655440102', 65.00, 'JUNIOR'),
('550e8400-e29b-41d4-a716-446655440103', 75.00, 'PLENO'),
('550e8400-e29b-41d4-a716-446655440104', 95.00, 'SENIOR'),
('550e8400-e29b-41d4-a716-446655440105', 70.00, 'PLENO')
ON CONFLICT DO NOTHING;