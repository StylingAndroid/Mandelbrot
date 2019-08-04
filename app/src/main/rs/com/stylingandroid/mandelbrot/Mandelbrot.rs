#pragma version(1)
#pragma rs java_package_name(com.stylingandroid.mandelbrot)

#include "hsv.rsh"

const double BASE_START_X = -2.0;
const double BASE_END_X = 1.0;
const double BASE_START_Y = -1.2;
const double BASE_END_Y = 1.2;

float width;
float height;
int32_t iterations;

double startX = BASE_START_X;
double startY = BASE_START_Y;
double endX = BASE_END_X;
double endY = BASE_END_Y;

void mandelbrot(
    rs_script script,
    rs_allocation allocation,
    int32_t iterations_value
) {
    width = rsAllocationGetDimX(allocation);
    height = rsAllocationGetDimY(allocation);
    double canvasWidth = (BASE_END_X - BASE_START_X);
    double canvasHeight = (BASE_END_Y - BASE_START_Y);
    double canvasRatio =  canvasWidth / canvasHeight;
    double imageRatio = width / height;
    if (canvasRatio > imageRatio) {
        double scaleFactor = canvasRatio / imageRatio;
        startX = BASE_START_X;
        endX = BASE_END_X;
        startY = BASE_START_Y * scaleFactor;
        endY = startY + (canvasHeight * scaleFactor);
    } else {
        double scaleFactor = imageRatio / canvasRatio;
        startX = BASE_START_X * scaleFactor;
        endX = startX + (canvasWidth * scaleFactor);
        startY = BASE_START_Y ;
        endY = BASE_END_Y;
    }
    iterations = iterations_value;
    rsForEach(script, allocation, allocation);
}

static uchar4 getColour(int32_t value) {
    float3 hsv;
    if (value < iterations) {
        hsv.x = 360.0 * (double)value / (double)iterations;
        hsv.y = 1.0;
        hsv.z = 1.0;
    } else {
        hsv.x = 0.0;
        hsv.y = 1.0;
        hsv.z = 0.0;
    }
    return rsPackColorTo8888(hsv2Argb(hsv, 255.0));
}

uchar4 RS_KERNEL root(uchar4 in, int32_t x, int32_t y) {
    double px = startX + ((double)x / width) * (endX - startX);
    double py = startY + ((double)y / height) * (endY - startY);
    double zr = 0.0;
    double zi = 0.0;
    double zrSquared = 0.0;
    double ziSquared = 0.0;
    int32_t n = 0;
    while (n <= iterations && (zrSquared + ziSquared) < 4.0) {
        zrSquared = zr * zr;
        ziSquared = zi * zi;
        zi *= zr;
        zi += zi + py;
        zr = zrSquared - ziSquared + px;
        n++;
    }
    return getColour(n);
}
