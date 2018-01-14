# -*- coding: utf-8 -*-
import csv

import numpy as np
import matplotlib.pyplot as plt
import sys
from matplotlib import animation
from numpy import genfromtxt


class TemperaturePhaseData:
    def __init__(self, timeTotal, timePhase, phase, average, max, min, core2SurfaceDelta, iterationDeltaTemp):
        self.timeTotal = timeTotal
        self.timePhase = timePhase
        self.phase = phase
        self.average = float(average)
        self.max = float(max)
        self.min = float(min)
        self.core2SurfaceDelta = float(core2SurfaceDelta)
        self.iterationDeltaTemp = float(iterationDeltaTemp)


def main(**kwargs):
    filenameBase = 'resources/output/2018-01-14-171246'
    if 'filename' in kwargs:
        filenameBase = kwargs['filename']
    width = 50
    if 'width' in kwargs:
        width = int(kwargs['width'])
    height = 10
    if 'height' in kwargs:
        height = int(kwargs['height'])

    filename = filenameBase + '.csv'
    temperatureFilename = filenameBase + '-temperature.csv'

    xData, avgT, minT, maxT, core2surfaceT, deltaT = [], [], [], [], [], []

    def loadTemperaturePhaseData(filename):
        with open(filename, 'rt') as csvfile:
            reader = csv.reader(csvfile, delimiter=',')
            next(reader)
            arr = []
            for row in reader:
                arr.append(TemperaturePhaseData(row[0], row[1], row[2], row[3], row[4], row[6], row[5], row[7]))
            return arr

    temperatureData = loadTemperaturePhaseData(temperatureFilename)
    data = genfromtxt(filename, delimiter=',')[:, 2:]

    for j in range(0, len(temperatureData)):
        xData.append(j)
        avgT.append(temperatureData[j].average)
        minT.append(temperatureData[j].min)
        maxT.append(temperatureData[j].max)
        core2surfaceT.append(temperatureData[j].core2SurfaceDelta)
        deltaT.append(temperatureData[j].iterationDeltaTemp)

    fig = plt.figure(1, figsize=(16, 9), dpi=100)
    fig.suptitle('Current phase: ' + temperatureData[0].phase + '\nTime: ' + temperatureData[0].timeTotal)
    minTemp = fig.add_subplot(521)
    minTLine, = minTemp.plot(minT)
    minTemp.set_yticks([min(minT), max(minT)])
    minTemp.set_title("Minimal temperature")

    maxTemp = fig.add_subplot(522)
    maxTLine, = maxTemp.plot(maxT)
    maxTemp.set_yticks([round(min(maxT), 2), round(max(maxT), 2)])
    maxTemp.set_title("Maximal temperature")

    avgTemp = fig.add_subplot(523)
    avgTempLine, = avgTemp.plot(avgT)
    avgTemp.set_yticks([round(min(avgT), 2), round(max(avgT), 2)])
    avgTemp.set_title("Average temperature")

    core2surface = fig.add_subplot(524)
    core2surfaceLine, = core2surface.plot(xData, core2surfaceT)
    core2surface.set_yticks([round(min(core2surfaceT), 2), round(max(core2surfaceT), 2)])
    core2surface.set_title("Difference between surface and core")

    deltaTemp = fig.add_subplot(525)
    deltaTLine, = deltaTemp.plot(xData, deltaT)
    deltaTemp.set_yticks([round(min(deltaT), 2), 0, round(max(deltaT), 2)])
    deltaTemp.set_title("Temperature delta since last iteration")
    deltaTemp.grid(True)

    heatDistribution = fig.add_subplot(2, 1, 2)
    heatDistributionImage = plt.imshow(np.reshape(data[0], [height, width]), cmap=plt.get_cmap('jet'), vmin=0, vmax=1200)
    heatDistribution.set_title('Heat distribution in element', fontsize=14, fontweight='bold')
    heatDistribution.set_xlabel('Element width')
    heatDistribution.set_ylabel('Element height')

    plt.colorbar(heatDistributionImage)
    heatDistribution.grid(True)
    plt.subplots_adjust(top=0.85, wspace=0.25, hspace=1)
    plt.inferno()
    fig.show()

    def updatefig(j):
        # set the data in the axesimage object
        fig.suptitle('Current phase: ' + temperatureData[j].phase + '\nTime: ' +
            temperatureData[j].timeTotal + '\n TempAvg: ' + str(
            round(temperatureData[j].average, 2)) + "\N{DEGREE SIGN}C   TempMax:" +
            str(round(temperatureData[j].max, 2)) + "\N{DEGREE SIGN}C    TempMin: " +
            str(round(temperatureData[j].min, 2)) + "\N{DEGREE SIGN}C\n Diffrence between core and surface: " +
            str(round(temperatureData[j].core2SurfaceDelta,2)) + "\N{DEGREE SIGN}C\nAvgerage delta temperature per minute: " +
            str(round(temperatureData[j].iterationDeltaTemp, 2)) + "\N{DEGREE SIGN}C")
        avgTempLine.set_data(xData[:j], avgT[:j])
        minTLine.set_data(xData[:j], minT[:j])
        maxTLine.set_data(xData[:j], maxT[:j])
        core2surfaceLine.set_data(xData[:j], core2surfaceT[:j])
        deltaTLine.set_data(xData[:j], deltaT[:j])
        heatDistributionImage.set_array(np.reshape(data[j], [height, width]))
        # return the artists set
        return [avgTempLine, minTLine, maxTLine, core2surfaceLine, deltaTLine, heatDistributionImage]

    # kick off the animation
    ani = animation.FuncAnimation(fig, updatefig, frames=range(len(data)), interval=1, blit=False)
    plt.show()


if __name__ == '__main__':
    main(**dict(arg.split('=') for arg in sys.argv[1:]))
