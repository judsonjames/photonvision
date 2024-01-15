###
# Alternative ARM Runner installer to setup PhotonVision JAR
# for ARM based builds such as Raspberry Pi, Orange Pi, etc.
# This assumes that the image provided to arm-runner-action contains
# the servicefile needed to auto-launch PhotonVision.
###

# This logic is needed for edge cases
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <IMAGE_SUFFIX>"
    exit 1
fi

NEW_JAR=$(realpath $(find . -name photonvision\*-linuxarm64.jar))
echo "Using jar: " $(basename $NEW_JAR)

DEST_PV_LOCATION=/opt/photonvision
sudo mkdir -p $DEST_PV_LOCATION
sudo cp $NEW_JAR ${DEST_PV_LOCATION}/photonvision.jar



IMAGE_SUFFIX="$1"

# Edge case logic for LL3
if [ "$IMAGE_SUFFIX" = "limelight3" ]; then
    sudo wget https://datasheets.raspberrypi.org/cmio/dt-blob-cam1.bin -O /boot/dt-blob.bin
    echo "dtoverlay=ov5647" | sudo tee -a /boot/config.txt
fi
