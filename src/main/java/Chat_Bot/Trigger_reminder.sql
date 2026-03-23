DELIMITER $$

CREATE TRIGGER check_reminder_datetime_before_insert
BEFORE INSERT ON reminder
FOR EACH ROW
BEGIN
    DECLARE reminder_datetime DATETIME;

    -- Combine date and time into a single DATETIME value
    SET reminder_datetime = STR_TO_DATE(CONCAT(NEW.reminder_date, ' ', NEW.reminder_time), '%Y-%m-%d %H:%i:%s');

    -- Compare with current datetime
    IF reminder_datetime < NOW() THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Cannot insert reminder: date and time already passed';
    END IF;
END$$

DELIMITER ;

