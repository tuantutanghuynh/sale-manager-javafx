-- V1__init.sql — reference tables, customers and sales reps.
--
-- Scope note: products, price tiers, orders, invoices and receivables are NOT here.
-- They arrive in later migrations together with the screens that use them (M3, M5, M6),
-- so that every migration ships with code that exercises it.
--
-- Vietnamese sorting: every human-readable name column declares
-- COLLATE "vi-VN-x-icu" (ADR-27). Without it, ORDER BY puts "Đ" after "Z" and
-- separates "Ă" from "A", which makes a customer list look scrambled.

-- ---------------------------------------------------------------------------
-- updated_at is maintained by a trigger rather than by each repository.
-- A repository that forgets to set it produces a silent data-quality bug that
-- nothing fails on; a trigger cannot be forgotten.
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- ---------------------------------------------------------------------------
-- Administrative units: province/city and ward. Two levels only — the 2025
-- reorganisation removed the district level (ADR-12).
--
-- There is no "code" column on purpose: the post-merger official codes are not
-- something this project can assert reliably, and the Excel files to be imported
-- carry names, not codes. Name is therefore the natural key. A code column can be
-- added in a later migration if an import file turns out to need one.
-- ---------------------------------------------------------------------------
CREATE TABLE province (
    id         SERIAL PRIMARY KEY,
    name       TEXT COLLATE "vi-VN-x-icu" NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_province_updated_at BEFORE UPDATE ON province
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Seeded empty and filled in as real customers appear (ADR-28). A ward name is only
-- unique inside its province, hence the composite unique key.
CREATE TABLE ward (
    id          SERIAL PRIMARY KEY,
    province_id INTEGER NOT NULL REFERENCES province (id),
    name        TEXT COLLATE "vi-VN-x-icu" NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (province_id, name)
);

CREATE INDEX idx_ward_province ON ward (province_id);

CREATE TRIGGER trg_ward_updated_at BEFORE UPDATE ON ward
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- Sales channel — "khách tỉnh", "đại lý", "hotel", "MT", ... (ADR-11).
-- This is the axis that decides the price tier and the debt policy. It is NOT the
-- A/B/C rank, which lives on customer and comes from the company.
--
-- The policy columns are the DEFAULT for the channel. A customer may override any
-- of them; see the matching nullable columns on customer.
-- ---------------------------------------------------------------------------
CREATE TABLE customer_group (
    id                   SERIAL PRIMARY KEY,
    code                 TEXT NOT NULL UNIQUE,
    name                 TEXT COLLATE "vi-VN-x-icu" NOT NULL,

    -- Money is NUMERIC(15,0) throughout: VND has no sub-unit, and NUMERIC keeps
    -- arithmetic exact where a float would drift (CLAUDE.md section 6).
    credit_limit         NUMERIC(15, 0),

    -- Contract payment term in days. The date a customer actually promises to pay
    -- is tracked per invoice in a later migration (ADR-16).
    payment_term_days    INTEGER,

    -- How many days before the due date to start reminding. The user sets this,
    -- there is no company-wide default worth hard-coding.
    reminder_days_before INTEGER,

    -- Minimum remaining shelf life, in days, this channel will accept.
    min_shelf_life_days  INTEGER,

    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_customer_group_updated_at BEFORE UPDATE ON customer_group
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- Sales reps under my supervision. Never deleted — a rep who leaves is marked
-- inactive so that reports for past periods keep resolving their name (ADR-14).
-- ---------------------------------------------------------------------------
CREATE TABLE sales_rep (
    id         SERIAL PRIMARY KEY,
    code       TEXT NOT NULL UNIQUE,
    name       TEXT COLLATE "vi-VN-x-icu" NOT NULL,
    is_active  BOOLEAN NOT NULL DEFAULT TRUE,
    joined_at  DATE,
    left_at    DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_sales_rep_updated_at BEFORE UPDATE ON sales_rep
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- Customer. Two independent classification axes (ADR-11):
--   group_id  -> sales channel, drives price tier and debt policy
--   rank      -> A/B/C, assigned by the company, drives priority ordering
--
-- sales_rep_id is who OWNS the customer. Who wrote a given order is a separate
-- column on sales_order in a later migration — a rep covering for a colleague is
-- routine, and coverage metrics need the owner while revenue needs the writer.
--
-- The three policy columns are NULL by default, meaning "inherit from the channel".
-- NULL is the right marker here: 0 is a legitimate credit limit, so a sentinel
-- value could not be told apart from a real one.
-- ---------------------------------------------------------------------------
CREATE TABLE customer (
    id                   SERIAL PRIMARY KEY,
    code                 TEXT NOT NULL UNIQUE,
    name                 TEXT COLLATE "vi-VN-x-icu" NOT NULL,

    group_id             INTEGER REFERENCES customer_group (id),
    province_id          INTEGER REFERENCES province (id),
    ward_id              INTEGER REFERENCES ward (id),
    sales_rep_id         INTEGER REFERENCES sales_rep (id),

    rank                 CHAR(1) CHECK (rank IN ('A', 'B', 'C')),

    credit_limit         NUMERIC(15, 0),
    payment_term_days    INTEGER,
    reminder_days_before INTEGER,

    phone                TEXT,
    address              TEXT COLLATE "vi-VN-x-icu",
    note                 TEXT,
    is_active            BOOLEAN NOT NULL DEFAULT TRUE,

    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_customer_group ON customer (group_id);
CREATE INDEX idx_customer_province ON customer (province_id);
CREATE INDEX idx_customer_ward ON customer (ward_id);
CREATE INDEX idx_customer_sales_rep ON customer (sales_rep_id);

CREATE TRIGGER trg_customer_updated_at BEFORE UPDATE ON customer
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- Every tunable number in the application lives here rather than in code, so that
-- a threshold can be changed without a rebuild (spec section 2, rule 6).
-- ---------------------------------------------------------------------------
CREATE TABLE app_setting (
    key         TEXT PRIMARY KEY,
    value       TEXT NOT NULL,
    description TEXT,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TRIGGER trg_app_setting_updated_at BEFORE UPDATE ON app_setting
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ---------------------------------------------------------------------------
-- Seed: how revenue is computed (spec section 9.1, ADR-21).
-- All three are switchable because the company's own rule may differ from the
-- default and may change.
-- ---------------------------------------------------------------------------
INSERT INTO app_setting (key, value, description) VALUES
    ('sales.date_basis', 'INVOICE_DATE',
     'Which date a sale counts on: ORDER_DATE or INVOICE_DATE'),
    ('sales.deduct_returns', 'true',
     'Subtract returned goods from revenue: true or false'),
    ('sales.vat_basis', 'INCL_VAT',
     'Revenue measured before or after VAT: EXCL_VAT or INCL_VAT');


-- ---------------------------------------------------------------------------
-- Seed: the 34 provincial-level units (ADR-28).
--
-- !! VERIFY BEFORE RUNNING !!
-- This list reflects the 2025 reorganisation to the best of the author's
-- knowledge, which is NOT an authoritative source. Check every name against an
-- official list first. A wrong name here becomes wrong data in every customer
-- record and every regional report, and fixing it later means UPDATE statements
-- against live data instead of an edit to this file.
-- ---------------------------------------------------------------------------
INSERT INTO province (name) VALUES
    -- 6 centrally-governed cities
    ('Hà Nội'),
    ('Hải Phòng'),
    ('Huế'),
    ('Đà Nẵng'),
    ('Hồ Chí Minh'),
    ('Cần Thơ'),
    -- 28 provinces
    ('Lai Châu'),
    ('Điện Biên'),
    ('Sơn La'),
    ('Lào Cai'),
    ('Tuyên Quang'),
    ('Thái Nguyên'),
    ('Cao Bằng'),
    ('Lạng Sơn'),
    ('Quảng Ninh'),
    ('Bắc Ninh'),
    ('Phú Thọ'),
    ('Hưng Yên'),
    ('Ninh Bình'),
    ('Thanh Hóa'),
    ('Nghệ An'),
    ('Hà Tĩnh'),
    ('Quảng Trị'),
    ('Quảng Ngãi'),
    ('Gia Lai'),
    ('Đắk Lắk'),
    ('Khánh Hòa'),
    ('Lâm Đồng'),
    ('Đồng Nai'),
    ('Tây Ninh'),
    ('Đồng Tháp'),
    ('An Giang'),
    ('Vĩnh Long'),
    ('Cà Mau');
