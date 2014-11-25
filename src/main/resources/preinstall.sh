#!/bin/sh
USERNAME=prefssvc
GROUP=prefssvc
HOME_DIR=/usr/share/preferences-service
getent group $GROUP >/dev/null || groupadd -r $GROUP
getent passwd $USERNAME >/dev/null || useradd -r -g $USERNAME -s /sbin/nologin -d $HOME_DIR -c "Rackspace Cloud Feeds Preferences Service" $USERNAME
