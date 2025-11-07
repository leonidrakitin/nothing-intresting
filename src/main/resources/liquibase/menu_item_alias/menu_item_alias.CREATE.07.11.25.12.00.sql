-- Создание таблицы соответствий alias -> menu_item/item_combo
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.sequences
        WHERE sequence_name = 'menu_item_alias_id_seq_generator'
    ) THEN
        CREATE SEQUENCE menu_item_alias_id_seq_generator START WITH 1 INCREMENT BY 1;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_name = 'menu_item_alias'
    ) THEN
        CREATE TABLE menu_item_alias (
            id BIGINT PRIMARY KEY DEFAULT nextval('menu_item_alias_id_seq_generator'),
            alias_text VARCHAR(512) NOT NULL UNIQUE,
            menu_item_id BIGINT,
            combo_id BIGINT,
            created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
            CONSTRAINT chk_menu_item_alias_target
                CHECK ((menu_item_id IS NOT NULL AND combo_id IS NULL)
                    OR (menu_item_id IS NULL AND combo_id IS NOT NULL))
        );

        ALTER TABLE menu_item_alias
            ADD CONSTRAINT fk_menu_item_alias_menu_item
                FOREIGN KEY (menu_item_id) REFERENCES menu_item (id) ON DELETE SET NULL;

        ALTER TABLE menu_item_alias
            ADD CONSTRAINT fk_menu_item_alias_combo
                FOREIGN KEY (combo_id) REFERENCES item_combo (id) ON DELETE SET NULL;
    END IF;
END $$;

