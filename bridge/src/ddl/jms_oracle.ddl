DROP TABLE oncempiJMSState;
DROP TABLE oncempiJMSStore;

CREATE TABLE oncempiJMSState (recordHandle int, recordState int, recordGeneration int);
CREATE TABLE oncempiJMSStore (recordHandle int, recordState int, record LONG RAW);

CREATE INDEX oncempiJMSMSG_X ON oncempiJMSState (recordHandle);
CREATE INDEX oncempiJMSMSGQ_X ON oncempiJMSStore (recordHandle);
COMMIT;
