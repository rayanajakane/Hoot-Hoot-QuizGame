import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import * as chartParameters from '@app/constants/chart-parameters';
import { HistogramService } from '@app/services/histogram/histogram.service';
import { ChoiceTally } from '@common/interfaces/choice-tally';
import { Histogram, MultipleChoiceHistogram } from '@common/interfaces/histogram';
import { AgChartOptions } from 'ag-charts-community';
import { Subscription } from 'rxjs/internal/Subscription';

@Component({
    selector: 'app-histogram',
    templateUrl: './histogram.component.html',
    styleUrls: ['./histogram.component.scss'],
})
export class HistogramComponent implements OnInit, OnChanges, OnDestroy {
    @Input() isResultsPage: boolean = false;
    @Input() currentHistogram: MultipleChoiceHistogram = {} as MultipleChoiceHistogram;
    currentQuestion: string;
    chartOptions: AgChartOptions = {};
    choiceTally: ChoiceTally[] = [];
    histogramsGame: MultipleChoiceHistogram[] = [];
    private histogramSubscriptions: Subscription[] = [];

    constructor(private readonly histogramService: HistogramService) {}

    subscribeToCurrentHistogram() {
        const currentHistogramSubscription = this.histogramService.currentHistogram$.subscribe((data: Histogram) => {
            const multipleChoiceData = data as MultipleChoiceHistogram;
            this.currentQuestion = multipleChoiceData.question;
            this.choiceTally = multipleChoiceData.choiceTallies;
            const dataTally = this.setUpData();
            this.setupChart(dataTally);
        });
        this.histogramSubscriptions.push(currentHistogramSubscription);
    }

    ngOnInit(): void {
        if (!this.isResultsPage) {
            this.histogramService.onCurrentHistogram();
            this.subscribeToCurrentHistogram();
        } else {
            this.choiceTally = this.currentHistogram.choiceTallies;
            this.currentQuestion = this.currentHistogram.question;
            const dataTally = this.setUpData();
            this.setupChart(dataTally);
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.currentQuestion || changes.currentHistogram) {
            this.resetChart();
            this.ngOnInit();
        }
    }

    ngOnDestroy(): void {
        this.histogramSubscriptions.forEach((subscription) => subscription.unsubscribe());
    }

    resetChart(): void {
        this.chartOptions = {};
        this.choiceTally = [];
    }

    setUpData() {
        return this.choiceTally.map((element, index) => ({
            label: `C${index + 1} ${element.isCorrect ? '✅' : '❌'}`,
            text: element.text,
            picks: element.tally,
        }));
    }

    // AG Charts requires using any; using unknown will cause compilation errors
    /* eslint-disable @typescript-eslint/no-explicit-any */

    renderChart(params: any) {
        return {
            content: `Choix: ${params.datum.text} <br/> Sélections: ${params.datum.picks}`,
        };
    }

    formatChart(params: any) {
        const fill = params.datum[params.xKey].includes('✅') ? 'green' : 'red';
        return { fill };
    }

    private setupChart(data: any): void {
        this.chartOptions = {
            title: { text: this.currentQuestion },
            axes: chartParameters.HISTOGRAM_AXES,
            data,
            series: [
                {
                    type: chartParameters.TYPE_BAR,
                    xKey: chartParameters.XKEY_LABEL,
                    xName: chartParameters.XNAME_CHOICE,
                    yKey: chartParameters.YKEY_PICKS,
                    yName: chartParameters.YNAME_CHOICE,
                    tooltip: {
                        enabled: true,
                        renderer: this.renderChart.bind(this),
                    },
                    formatter: this.formatChart.bind(this),
                },
            ],
        };
    }
}
