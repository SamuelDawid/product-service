CREATE TABLE product_variant_option_values (
                                               variant_id      BIGINT NOT NULL,
                                               option_value_id BIGINT NOT NULL,

    -- złożony PK: ta sama wartość nie da się przypisać do wariantu dwa razy
                                               CONSTRAINT pk_product_variant_option_values PRIMARY KEY (variant_id, option_value_id),
                                               CONSTRAINT fk_variant_option_value_variant FOREIGN KEY (variant_id) REFERENCES product_variants (id),
                                               CONSTRAINT fk_variant_option_value_value FOREIGN KEY (option_value_id) REFERENCES product_option_values (id)
);

-- PK pokrywa wyszukiwanie po variant_id, druga strona potrzebuje własnego indeksu
CREATE INDEX idx_variant_option_values_value_id ON product_variant_option_values (option_value_id);