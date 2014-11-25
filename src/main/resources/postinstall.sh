#!/bin/sh
USERNAME=prefssvc
GROUP=prefssvc
LOG_DIR=/var/log/preferences-service
/sbin/chkconfig --add preferences-service
test -d $LOG_DIR || ( mkdir $LOG_DIR && chown $USERNAME:$GROUP $LOG_DIR )
