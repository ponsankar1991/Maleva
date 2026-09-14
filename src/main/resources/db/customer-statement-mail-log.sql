-- Customer Statement of Account: one row per statement mail, sent or refused.
-- Read by the Send Statements screen for "last sent" (so a customer is not
-- mailed twice in one round) and written by every send, single or bulk.
--
-- The application creates this table at startup when it is missing
-- (mail.statement.log.auto-create, default true). Run this by hand where the
-- app's SQL login may not create tables, and set that property to false.
IF OBJECT_ID('dbo.CustomerStatementMailLog', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.CustomerStatementMailLog (
        Id             BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_CustomerStatementMailLog PRIMARY KEY,
        CompanyRefId   INT            NOT NULL,
        CustomerRefId  INT            NOT NULL,
        CustomerName   NVARCHAR(200)  NULL,
        SentTo         NVARCHAR(1000) NOT NULL,
        Cc             NVARCHAR(500)  NULL,
        Subject        NVARCHAR(300)  NULL,
        Reminder       NVARCHAR(50)   NULL,          -- '' statement, 'Reminder 1', 'Reminder 2'
        StatementDate  DATE           NULL,
        OverdueAmount  DECIMAL(18,2)  NULL,
        Currency       NVARCHAR(10)   NULL,
        AttachmentName NVARCHAR(200)  NULL,
        Status         VARCHAR(10)    NOT NULL,      -- SENT / FAILED
        Error          NVARCHAR(1000) NULL,
        JobId          VARCHAR(40)    NULL,          -- bulk run id, NULL for a single send
        SentBy         NVARCHAR(100)  NULL,
        SentAt         DATETIME2(0)   NOT NULL,
        Kind           VARCHAR(10)    NOT NULL CONSTRAINT DF_CSML_Kind DEFAULT 'STATEMENT',  -- STATEMENT | REPLY
        MessageId      NVARCHAR(255)  NULL,          -- our Message-ID; what a customer's reply points back to
        InReplyTo      NVARCHAR(255)  NULL,          -- for a reply we send: the customer mail it answers
        ReplyToRefId   BIGINT         NULL,          -- for a reply we send: the CustomerStatementMailReply row
        BodyText       NVARCHAR(MAX)  NULL           -- the message content sent
    );
    CREATE INDEX IX_CustomerStatementMailLog_Customer
        ON dbo.CustomerStatementMailLog (CompanyRefId, CustomerRefId, SentAt DESC);
    CREATE INDEX IX_CustomerStatementMailLog_MessageId
        ON dbo.CustomerStatementMailLog (MessageId);
END

-- Tables created before the reply feature: add the five columns.
IF COL_LENGTH('dbo.CustomerStatementMailLog', 'Kind') IS NULL
    ALTER TABLE dbo.CustomerStatementMailLog ADD Kind VARCHAR(10) NOT NULL CONSTRAINT DF_CSML_Kind DEFAULT 'STATEMENT';
IF COL_LENGTH('dbo.CustomerStatementMailLog', 'MessageId') IS NULL
    ALTER TABLE dbo.CustomerStatementMailLog ADD MessageId NVARCHAR(255) NULL;
IF COL_LENGTH('dbo.CustomerStatementMailLog', 'InReplyTo') IS NULL
    ALTER TABLE dbo.CustomerStatementMailLog ADD InReplyTo NVARCHAR(255) NULL;
IF COL_LENGTH('dbo.CustomerStatementMailLog', 'ReplyToRefId') IS NULL
    ALTER TABLE dbo.CustomerStatementMailLog ADD ReplyToRefId BIGINT NULL;
IF COL_LENGTH('dbo.CustomerStatementMailLog', 'BodyText') IS NULL
    ALTER TABLE dbo.CustomerStatementMailLog ADD BodyText NVARCHAR(MAX) NULL;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_CustomerStatementMailLog_MessageId')
    CREATE INDEX IX_CustomerStatementMailLog_MessageId ON dbo.CustomerStatementMailLog (MessageId);
