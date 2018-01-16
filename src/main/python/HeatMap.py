# -*- coding: utf-8 -*-
import csv

import numpy as np
import matplotlib.pyplot as plt
import sys
from matplotlib import animation
from numpy import genfromtxt
import time
import math


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
    filenameBase = 'resources/output/2018-01-15-215539'
    if 'filename' in kwargs:
        filenameBase = kwargs['filename']
    width = 50
    if 'width' in kwargs:
        width = int(kwargs['width'])
    height = 10
    if 'height' in kwargs:
        height = int(kwargs['height'])
    live = False
    if 'live' in kwargs:
        live = kwargs['live'].lower() in ("yes", "true", "t", "1")
    filename = filenameBase + '.csv'
    temperatureFilename = filenameBase + '-temperature.csv'

    temperatureData, data, xData, avgT, minT, maxT, core2surfaceT, deltaT, timeTotal = [], [], [], [],[], [], [], [], []
    def loadCSV():
        try:
            _tData = loadTemperaturePhaseData()
            _data = genfromtxt(filename, delimiter=',')[:, 2:]
            return _tData, _data
        except IndexError:
            time.sleep(1)
            return loadCSV()
    def loadTemperaturePhaseData():
        with open(temperatureFilename, 'rt') as csvfile:
            reader = csv.reader(csvfile, delimiter=',')
            try:
                next(reader)
            except:
                time.sleep(1)
                return loadTemperaturePhaseData()
            arr = []
            for row in reader:
                arr.append(TemperaturePhaseData(row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7]))
            return arr

    def load():
        _xData, _avgT, _minT, _maxT, _core2surfaceT, _deltaT, _time = [], [], [], [], [], [], []

        _tData, _data = loadCSV()

        for j in range(0, len(_tData)):
            _xData.append(j)
            _avgT.append(_tData[j].average)
            _minT.append(_tData[j].min)
            _maxT.append(_tData[j].max)
            _core2surfaceT.append(_tData[j].core2SurfaceDelta)
            _deltaT.append(_tData[j].iterationDeltaTemp)
            _time.append(_tData[j].timeTotal)
        return _tData, _data, _xData, _avgT, _minT, _maxT, _core2surfaceT, _deltaT, _time

    if not live:
        temperatureData, data, xData, avgT, minT, maxT, core2surfaceT, deltaT, timeTotal = load()

    fig = plt.figure(1, figsize=(16, 9), dpi=100)
    fig.suptitle('Current phase: null\nTime: 00h 00m 00s \nLiveMode: ' + str(live))
    minTemp = fig.add_subplot(521)
    minTLine, = minTemp.plot(minT)
    minTemp.set_title("Minimal temperature")

    maxTemp = fig.add_subplot(522)
    maxTLine, = maxTemp.plot(maxT)
    maxTemp.set_title("Maximal temperature")

    avgTemp = fig.add_subplot(523)
    avgTempLine, = avgTemp.plot(avgT)
    avgTemp.set_title("Average temperature")

    core2surface = fig.add_subplot(524)
    core2surfaceLine, = core2surface.plot(xData, core2surfaceT)
    core2surface.set_title("Difference between surface and core")

    deltaTemp = fig.add_subplot(525)
    deltaTLine, = deltaTemp.plot(xData, deltaT)
    deltaTemp.set_title("Temperature delta since last iteration")
    minTemp.grid(True)
    maxTemp.grid(True)
    deltaTemp.grid(True)
    avgTemp.grid(True)
    core2surface.grid(True)

    if not live:
        xTicks=[]
        labels=[]
        j=len(temperatureData)
        for i in range(0, j, max(math.ceil(0.1*j),2)):
            xTicks.append(i)
            labels.append(timeTotal[i])
        minTemp.set_yticks([min(minT), max(minT)+1])
        minTemp.set_xticks(xTicks)
        minTemp.set_xticklabels(labels, rotation=20)

        maxTemp.set_yticks([round(min(maxT), 2), round(max(maxT)+1, 2)])
        maxTemp.set_xticks([0, j])
        maxTemp.set_xticks(xTicks)
        maxTemp.set_xticklabels(labels, rotation=20)

        avgTemp.set_yticks([round(min(avgT), 2), round(max(avgT)+1, 2)])
        avgTemp.set_xticks([0, j])
        avgTemp.set_xticks(xTicks)
        avgTemp.set_xticklabels(labels, rotation=20)

        core2surface.set_yticks([round(min(core2surfaceT), 2), round(max(core2surfaceT)+1,2)])
        core2surface.set_xticks([0, j])
        core2surface.set_xticks(xTicks)
        core2surface.set_xticklabels(labels, rotation=20)

        deltaTemp.set_yticks([round(min(deltaT), 2), 0, round(max(deltaT)+1, 2)])
        deltaTemp.set_xticks([0, j])
        deltaTemp.set_xticks(xTicks)
        deltaTemp.set_xticklabels(labels, rotation=20)

    heatDistribution = fig.add_subplot(2, 1, 2)
    heatDistributionImage = plt.imshow(np.zeros((height, width)), cmap=plt.get_cmap('jet'), vmin=0, vmax=1200)
    heatDistribution.set_title('Heat distribution in element', fontsize=14, fontweight='bold')
    heatDistribution.set_xlabel('Element width')
    heatDistribution.set_ylabel('Element height')
    heatDistribution.grid(True)

    plt.colorbar(heatDistributionImage)
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

    def liveUpdateFig(j, iter=0):
        if iter>1000:
            return null
        tempData, d, xD, avgTe, minTe, maxTe, core2surfaceTe, deltaTe, t = load()
        j=min(int(j), len(tempData))

        if len(tempData) == 0 or j >=len(tempData):
            time.sleep(1)
            return liveUpdateFig(j, iter+1)

        fig.suptitle('Current phase: ' + tempData[j].phase + '\nTime: ' +
            tempData[j].timeTotal + '\n TempAvg: ' + str(
            round(tempData[j].average, 2)) + "\N{DEGREE SIGN}C   TempMax:" +
            str(round(tempData[j].max, 2)) + "\N{DEGREE SIGN}C    TempMin: " +
            str(round(tempData[j].min, 2)) + "\N{DEGREE SIGN}C\n Diffrence between core and surface: " +
            str(round(tempData[j].core2SurfaceDelta,2)) + "\N{DEGREE SIGN}C\nAvgerage delta temperature per minute: " +
            str(round(tempData[j].iterationDeltaTemp, 2)) + "\N{DEGREE SIGN}C")

        avgTempLine.set_data(xD[:j], avgTe[:j])
        minTLine.set_data(xD[:j], minTe[:j])
        maxTLine.set_data(xD[:j], maxTe[:j])
        core2surfaceLine.set_data(xD[:j], core2surfaceTe[:j])
        deltaTLine.set_data(xD[:j], deltaTe[:j])
        heatDistributionImage.set_array(np.reshape(d[j], [height, width]))

        xTicks=[]
        labels=[]
        for i in range(0, j, max(math.ceil(0.1*j),2)):
            xTicks.append(i)
            labels.append(t[i])
        minTemp.set_yticks([min(minTe), max(minTe)+1])
        minTemp.set_xticks(xTicks)
        minTemp.set_xticklabels(labels, rotation=20)

        maxTemp.set_yticks([round(min(maxTe), 2), round(max(maxTe)+1, 2)])
        maxTemp.set_xticks([0, j])
        maxTemp.set_xticks(xTicks)
        maxTemp.set_xticklabels(labels, rotation=20)

        avgTemp.set_yticks([round(min(avgTe), 2), round(max(avgTe)+1, 2)])
        avgTemp.set_xticks([0, j])
        avgTemp.set_xticks(xTicks)
        avgTemp.set_xticklabels(labels, rotation=20)

        core2surface.set_yticks([round(min(core2surfaceTe), 2), round(max(core2surfaceTe)+1,2)])
        core2surface.set_xticks([0, j])
        core2surface.set_xticks(xTicks)
        core2surface.set_xticklabels(labels, rotation=20)

        deltaTemp.set_yticks([round(min(deltaTe), 2), 0, round(max(deltaTe)+1, 2)])
        deltaTemp.set_xticks([0, j])
        deltaTemp.set_xticks(xTicks)
        deltaTemp.set_xticklabels(labels, rotation=20)

        # return the artists set
        return [avgTempLine, minTLine, maxTLine, core2surfaceLine, deltaTLine, heatDistributionImage]

    if not live:
        plt.rcParams['animation.ffmpeg_path'] = 'C:\\Users\\Wojci\\Documents\\GitHub\\AGH_FEM\\ffmpeg-3.4.1-win64-static\\bin\\ffmpeg.exe'
        Writer = animation.writers['ffmpeg']
        writer = Writer(fps=15, metadata=dict(artist='Wojciech Mazur'), bitrate=1800)
        ani = animation.FuncAnimation(fig, updatefig, frames=range(len(data)), interval=10, blit=False)
        ani.save(filenameBase+".mp4", writer=writer)
    else:
        ani = animation.FuncAnimation(fig, liveUpdateFig, interval=100, blit=False)
    plt.show()


if __name__ == '__main__':
    main(**dict(arg.split('=') for arg in sys.argv[1:]))
