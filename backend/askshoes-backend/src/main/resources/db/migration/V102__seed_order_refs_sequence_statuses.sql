INSERT INTO krn_ref_type (code, name) VALUES
    ('ITEM_TYPE', 'ТИП ИЗДЕЛИЯ'),
    ('SIZE_GRID', 'РАЗМЕРНАЯ СЕТКА'),
    ('ITEM_SIZE', 'РАЗМЕР'),
    ('MATERIAL', 'МАТЕРИАЛ'),
    ('CLIENT_SOURCE', 'ИСТОЧНИК КЛИЕНТА'),
    ('CONTACT_METHOD', 'СПОСОБ СВЯЗИ'),
    ('CLIENT_STATUS', 'СТАТУС КЛИЕНТА');

INSERT INTO krn_sequence (code, pattern, reset_period) VALUES
    ('ORDER', 'MSK-%s-%04d', 'DAILY');

INSERT INTO krn_status_type (code) VALUES ('ORDER');

INSERT INTO krn_status (status_type_id, code, name, sort_order)
SELECT t.id, s.code, s.name, s.sort_order
FROM krn_status_type t
CROSS JOIN (VALUES
    ('NEW', 'Новый', 10),
    ('ACCEPTED', 'Принят', 20),
    ('DIAGNOSTICS', 'Диагностика', 30),
    ('QUOTED', 'Смета согласуется', 40),
    ('APPROVED', 'Смета согласована', 50),
    ('IN_PROGRESS', 'В работе', 60),
    ('QUALITY_CHECK', 'Контроль качества', 70),
    ('READY', 'Готов к выдаче', 80),
    ('ISSUED', 'Выдан', 90),
    ('CLOSED', 'Закрыт', 100),
    ('REJECTED', 'Отказ', 110),
    ('CANCELLED', 'Отменён', 120),
    ('ON_HOLD', 'Приостановлен', 130)
) AS s(code, name, sort_order)
WHERE t.code = 'ORDER';
