-- Customers' replies to their statements, read from the accounts mailbox by
-- StatementReplyImporter, and where that reader left off. Run once per
-- database (the application does not create these itself).
IF OBJECT_ID('dbo.CustomerStatementMailReply', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.CustomerStatementMailReply (
        Id             BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_CustomerStatementMailReply PRIMARY KEY,
        CompanyRefId   INT            NOT NULL,
        CustomerRefId  INT            NOT NULL,
        MailLogRefId   BIGINT         NULL,          -- the statement (or our reply) this answers
        Mailbox        NVARCHAR(100)  NOT NULL,      -- the login it was read with
        Folder         NVARCHAR(100)  NOT NULL,      -- INBOX
        MailboxUid     BIGINT         NOT NULL,      -- IMAP UID: the same mail is never stored twice
        MessageId      NVARCHAR(255)  NULL,
        InReplyTo      NVARCHAR(255)  NULL,
        FromAddress    NVARCHAR(255)  NOT NULL,
        FromName       NVARCHAR(200)  NULL,
        ToAddresses    NVARCHAR(1000) NULL,
        CcAddresses    NVARCHAR(1000) NULL,
        Subject        NVARCHAR(500)  NULL,
        BodyText       NVARCHAR(MAX)  NULL,
        BodyHtml       NVARCHAR(MAX)  NULL,
        HasAttachments BIT            NOT NULL CONSTRAINT DF_CSMR_HasAtt DEFAULT 0,
        ReceivedAt     DATETIME2(0)   NOT NULL,
        ReadBy         NVARCHAR(100)  NULL,
        ReadAt         DATETIME2(0)   NULL,
        CreatedAt      DATETIME2(0)   NOT NULL CONSTRAINT DF_CSMR_Created DEFAULT SYSDATETIME(),
        CONSTRAINT UQ_CSMR_Mail UNIQUE (Mailbox, Folder, MailboxUid)
    );
    CREATE INDEX IX_CSMR_Customer ON dbo.CustomerStatementMailReply (CompanyRefId, CustomerRefId, ReceivedAt DESC);
END

IF OBJECT_ID('dbo.CustomerStatementMailAttachment', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.CustomerStatementMailAttachment (
        Id           BIGINT IDENTITY(1,1) NOT NULL CONSTRAINT PK_CustomerStatementMailAttachment PRIMARY KEY,
        ReplyRefId   BIGINT         NOT NULL CONSTRAINT FK_CSMA_Reply REFERENCES dbo.CustomerStatementMailReply (Id),
        FileName     NVARCHAR(255)  NOT NULL,
        ContentType  NVARCHAR(100)  NULL,
        SizeBytes    BIGINT         NOT NULL,
        StoragePath  NVARCHAR(500)  NOT NULL       -- relative to the upload directory
    );
END

IF OBJECT_ID('dbo.MailboxSyncState', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.MailboxSyncState (
        Mailbox      NVARCHAR(100)  NOT NULL,
        Folder       NVARCHAR(100)  NOT NULL,
        UidValidity  BIGINT         NOT NULL,       -- IMAP resets UIDs when this changes; then re-read from the start
        LastUid      BIGINT         NOT NULL,
        LastRunAt    DATETIME2(0)   NULL,
        LastError    NVARCHAR(1000) NULL,
        CONSTRAINT PK_MailboxSyncState PRIMARY KEY (Mailbox, Folder)
    );
END
