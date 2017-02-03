package ml.regression;

import org.junit.Test;
import org.mwg.*;
import org.mwg.internal.scheduler.NoopScheduler;
import org.mwg.ml.AbstractMLNode;
import org.mwg.mlx.MLXPlugin;
import org.mwg.mlx.algorithm.regression.AutoregressionBasedPeriodicityDetector;

import static org.junit.Assert.assertEquals;

/**
 * Created by andrey.boytsov on 09/08/16.
 */
public class AutoregressionBasedPeriodicityDetectorTest {
    double consumptionLondon[] = new double[]{64, 78, 96, 93, 92, 124, 87, 44, 25, 48, 87, 119, 78, 58, 60, 124, 279, 625, 115, 83, 1018, 1382, 950, 1266, 950, 2022,
            1181, 779, 245, 354, 192, 168, 137, 147, 1123, 915, 270, 861, 889, 258, 195, 185, 206, 194, 287, 155, 169, 92, 87, 56, 76, 42, 20, 58, 90, 67, 83, 45, 17,
            25, 93, 80, 71, 180, 116, 486, 202, 153, 116, 69, 63, 74, 129, 265, 103, 183, 126, 126, 85, 122, 68, 35, 30, 240, 1304, 741, 283, 283, 270, 271, 250, 228, 327,
            208, 188, 116, 87, 95, 89, 38, 18, 29, 59, 110, 102, 39, 18, 27, 56, 88, 93, 38, 33, 233, 432, 188, 111, 71, 83, 60, 172, 128, 269, 61, 64, 95, 65, 90, 92,
            166, 160, 343, 1087, 868, 671, 211, 306, 398, 638, 289, 312, 695, 583, 167, 107, 19, 22, 57, 55, 93, 104, 44, 18, 47, 54, 83, 76, 41, 29, 61, 280, 562, 181,
            521, 303, 272, 503, 530, 188, 118, 186, 131, 133, 133, 105, 309, 206, 183, 156, 172, 1237, 613, 200, 229, 364, 368, 233, 231, 346, 195, 68, 34, 58, 55, 56,
            101, 83, 39, 44, 57, 54, 53, 104, 44, 24, 57, 79, 53, 150, 161, 734, 179, 163, 690, 565, 182, 166, 199, 193, 169, 176, 144, 99, 125, 288, 125, 161, 299, 1173,
            606, 187, 239, 261, 190, 163, 199, 341, 203, 120, 66, 24, 17, 46, 98, 89, 78, 28, 18, 44, 86, 85, 76, 18, 18, 71, 239, 207, 451, 103, 94, 176, 111, 91, 237,
            229, 237, 563, 273, 567, 116, 149, 164, 278, 164, 280, 156, 165, 140, 142, 149, 196, 164, 160, 104, 125, 144, 166, 162, 126, 36, 21, 49, 56, 92, 105, 56, 19,
            32, 57, 86, 91, 52, 19, 38, 70, 321, 277, 468, 282, 595, 329, 123, 223, 74, 151, 179, 88, 58, 77, 79, 56, 30, 141, 123, 787, 1311, 490, 211, 170, 193, 247,
            282, 346, 168, 109, 60};

    double correctAutoregressionCoefs[] = new double[]{1, 0.63183242007981899, 0.39742449381945105,0.331997656566068, 0.30378943172955392, 0.1991113010522407,
            0.097581654794358916, 0.045533430281026639, 0.083018277317790268, 0.087386966400881672, 0.034402746271035768, -0.00029518711781846595,
            0.0074485585105999634, 0.0056780069789143109, 0.0068334939519373201, -0.033558982098773595, -0.070836633930822068, -0.032967530673629883,
            -0.025148575317837017, -0.042836140064440774, -0.090670401019243799, -0.11970960544219468, -0.13646547197320819, -0.1495842429490587,
            -0.15870448521423589, -0.14795614902435258, -0.14828943125385871, -0.128029346008007,  -0.10071667813362851, -0.069156266458362259,
            -0.078309569359931289, -0.12176767621469772, -0.1465473272566237, -0.12447895694687711, -0.13659846156440067, -0.14084597005912855,
            -0.13740325228235914, -0.10808959751812197, -0.027534994639553102, 0.0059995895542790025, 0.021311594756000161, -0.0014226189750919088,
            0.023858483414347015, 0.055737585918404546, 0.064511731166736336, 0.037495007291768911, 0.11558114068787789, 0.21985445903779025,
            0.26562236159038988, 0.20528447661150737, 0.16522182774646599, 0.071616514134571749, 0.061857151017235448, 0.053915942457553358,
            0.012479506412149297, -0.024474191241125074, -0.021089183299394795, 0.019044102960473586, 0.052214388235170328, 0.050081339305815091,
            -0.012720743199959991, -0.023827700666297406, -0.026298225425204543, -0.013170469409636969, -0.016341780197346559, -0.062322128748259265,
            -0.065300673447362304, -0.014036516772721041, -0.022862755422364905, -0.055710951932155417, -0.10073242490660658, -0.10612509068526661,
            -0.10229524665570337, -0.12920867232792735, -0.15142612737883351, -0.12219655050172716, -0.066050633065199857, -0.054507977113944554,
            -0.094528907580530688, -0.089047988190937566, -0.099721456312321113, -0.094861596808538187, -0.11234226458051531, -0.14838158231993426,
            -0.17563542783305083, -0.15596742772512934, -0.087868438618896857, -0.048363665145052227, 0.001178757679768337, -0.039216284614529591,
            -0.014789222244044106, 0.0076161375561863134, 0.0099217424896611318, 0.037156685792452265, 0.11533246796024282, 0.20253201217163969,
            0.32214665089311939, 0.24861592375061903, 0.11904617838213374, 0.088958979618708509, 0.057119327628260642, 0.0067331644253282924,
            -0.021695040365967581, -0.0045280508233851062, 0.017712681743116659, -0.002150204580516301, 0.0087601892601187857, 0.090832667423680513,
            0.044310536601389443, 0.006059237618125061, 0.0026087033483833161, 0.081919209289736006, 0.089470729289027565, 0.03434829119259776,
            0.016956194966511254, 0.091467630033587405, 0.061948737988001636, 0.021427617612846121, -0.042785798789719255, -0.077900051212460783,
            -0.10424779508071984, -0.094639726045507119, -0.11952314674532095, -0.11373925694213616, -0.090285628034750223, -0.051377095653626179,
            -0.028575485603981116, -0.086542513058216863, -0.098152411631125425, -0.031214079434904569, -0.080328631870448503, -0.14190432301604852,
            -0.11699846579926573, -0.11306414931758386, -0.065501927487875172, -0.033824115486886706, 0.019074189208942095, 0.01396121552535831,
            0.058235029509210987, 0.080076390246017537, 0.15935421488194948, 0.16821500231568684, 0.21071426876783464, 0.26907617895116753};

    int allPeriods[][] = new int[][]{{1,44,96,},
            {1,44,96,},{1,44,96,},{1,44,96,},{1,44,96,},{1,44,96,},
            {1,44,96,},{1,44,96,},{1,44,96,},{1,44,96,},{1,44,96,},
            {1,44,96,},{1,44,96,},{1,44,96,},{1,44,96,},{1,44,96,},
            {1,44,96,},{1,44,96,},{1,44,96,},{1,44,96,},{1,44,96,},
            {1,44,96,},{1,44,96,},{1,44,96,},{1,44,96,},{1,44,96,},
            {1,44,96,},{1,44,96,},{1,44,96,},{1,44,96,},{1,37,48,96,},
            {1,37,41,48,96,},{1,35,37,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,96,},
            {1,96,},{1,96,},{1,96,},{1,96,},{1,96,},
            {1,96,},{1,96,},{1,96,},{1,96,},{1,96,},
            {1,96,},{1,96,},{1,96,},{1,96,},{1,96,},
            {1,96,},{1,96,},{1,96,},{1,96,},{1,96,},
            {1,96,},{1,96,},{1,96,},{1,96,},{1,96,},
            {1,96,},{1,96,},{1,96,},{1,11,96,},{1,96,},
            {1,96,},{1,96,},{1,96,137,},{1,96,137,},{1,96,137,},
            {1,96,137,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},
            {1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},
            {1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,142,},{1,96,137,142,},
            {1,96,137,142,},{1,96,137,142,},{1,96,137,142,},{1,96,137,142,},{1,96,137,142,},
            {1,96,137,142,},{1,96,137,142,},{1,96,137,142,},{1,96,137,142,},{1,96,137,142,},
            {1,96,137,142,},{1,96,137,142,},{1,96,137,142,},{1,96,137,142,},{1,96,137,142,},
            {1,96,137,142,},{1,96,137,142,},{1,96,137,142,},{1,96,137,142,},{1,96,137,142,},
            {1,96,137,142,},{1,96,137,142,},{1,96,137,142,},{1,96,137,142,},{1,96,137,142,},
            {1,96,137,142,},{1,96,137,142,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},
            {1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},
            {1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},
            {1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},
            {1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},
            {1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},
            {1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},
            {1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},
            {1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},
            {1,94,143,},{1,94,143,},{1,94,143,},{1,94,143,},{1,94,143,},
            {1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},
            {1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},
            {1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,94,137,143,},
            {1,94,137,143,},{1,94,137,143,},{1,94,137,143,},{1,96,137,140,143,},{1,96,137,140,143,},
            {1,96,137,140,143,},{1,96,137,140,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},
            {1,96,137,143,},{1,96,137,140,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},
            {1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},
            {1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},
            {1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},
            {1,96,137,140,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},
            {1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},
            {1,96,137,143,},{1,96,137,},{1,96,137,},{1,96,137,},{1,96,137,},
            {1,96,137,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},{1,96,137,143,},
            {1,96,137,143,},{1,48,96,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,101,143,},{1,48,97,101,143,},{1,48,97,101,143,},
            {1,48,97,101,143,},{1,48,97,101,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,},{1,48,97,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,101,},
            {1,48,96,101,},{1,48,96,101,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,},
            {1,48,96,140,},{1,48,93,96,140,143,},{1,48,96,140,},{1,48,96,140,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,140,143,},{1,48,96,140,143,},{1,48,96,140,143,},{1,48,96,140,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,140,143,},{1,48,96,140,143,},{1,48,96,140,143,},{1,48,96,140,143,},
            {1,48,96,140,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,140,143,},{1,48,96,140,143,},
            {1,48,96,140,143,},{1,48,96,140,143,},{1,48,96,140,143,},{1,48,96,140,143,},{1,48,96,140,143,},
            {1,48,96,140,143,},{1,48,96,140,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,140,143,},{1,48,96,140,143,},{1,48,96,140,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,140,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},{1,48,96,},
            {1,48,96,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},
            {1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},
            {1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},
            {1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},
            {1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},
            {1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},
            {1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},
            {1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},
            {1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},
            {1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,48,97,143,},
            {1,48,97,143,},{1,48,97,143,},{1,48,97,143,},{1,49,96,143,},{1,49,96,143,},
            {1,49,97,143,},{1,49,97,143,},{1,49,97,143,},{1,49,97,140,143,},{1,49,97,143,},
            {1,49,97,143,},{1,49,97,143,},{1,49,97,143,},{1,49,97,143,},{1,49,97,143,},
            {1,49,97,143,},{1,49,97,143,},{1,49,96,143,},{1,49,96,143,},{1,49,96,143,},
            {1,49,96,143,},{1,49,97,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},{1,48,96,143,},
            {1,48,96,143,},{1,48,96,143,},{1,48,96,143,},};

    int allDistances[] = new int[]{0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,4,6,8,0,0,0,
            0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,0,
            0,4,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,6,0,0,0,4,0,0,0,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,
            4,0,0,0,0,0,0,0,0,0,4,0,0,0,0,4,0,0,0,0,8,1,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,1,0,
            0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,4,0,0,4,0,0,0,4,4,8,8,0,4,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,4,0,0,0,4,0,4,0,0,0,0,4,0,4,0,0,
            0,0,0,0,0,0,4,0,0,0,4,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,1,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,1,0,0,4,4,0,0,0,0,0,0,0,1,0,0,0,
            1,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};

    int allConsumption[] = new int[]{265,139,92,86,86,77,53,82,52,54,52,53,78,73,23,56,54,53,198,164,83,127,110,
            109,656,852,885,1299,939,1587,1406,1296,1241,1002,1123,1457,744,688,1487,1418,434,702,500,749,526,
            301,267,388,234,178,100,64,82,123,95,95,74,43,69,75,63,61,63,48,96,63,61,
            591,945,1392,1218,950,1265,1186,1438,1357,1980,222,292,196,227,150,112,233,292,568,718,1035,810,
            166,178,242,264,216,176,288,197,133,121,73,18,28,57,56,82,56,38,50,55,69,39,
            40,78,59,89,249,75,526,195,193,506,310,178,355,347,185,168,111,239,228,146,149,152,
            200,237,950,567,172,194,176,221,217,210,385,160,57,62,53,33,87,55,53,53,55,49,
            81,57,42,38,55,80,60,183,142,494,465,201,382,500,129,147,328,139,92,43,55,81,
            103,112,93,76,87,110,136,145,140,206,361,239,253,212,291,238,107,64,57,57,58,77,
            90,81,23,49,57,56,91,70,21,58,56,74,369,247,498,158,565,381,135,68,320,449,
            413,613,145,76,110,106,117,178,164,164,1235,1199,217,271,241,400,250,239,360,165,58,55,
            54,52,84,60,43,43,58,56,87,51,46,50,58,68,62,73,275,107,568,428,129,248,
            529,280,147,208,254,171,143,132,147,130,107,114,72,45,99,92,87,113,120,137,320,377,
            280,193,133,107,101,74,24,18,38,85,63,89,81,21,47,48,54,53,59,79,332,125,
            648,227,139,147,152,148,176,109,80,55,53,74,67,99,90,48,118,203,239,374,250,353,
            424,227,277,238,375,157,100,59,102,82,54,53,67,89,122,84,56,55,27,49,49,69,
            226,201,774,844,857,909,946,918,911,870,1161,1209,923,987,1156,1003,1223,573,437,398,264,176,
            193,910,711,202,553,224,493,574,396,227,101,18,51,58,82,70,81,42,25,76,71,56,
            59,56,36,90,56,124,150,464,258,302,78,57,68,108,118,209,99,162,90,149,171,143,
            181,159,159,308,1000,893,178,172,160,191,243,224,353,248,196,129,20,19,54,56,55,121,
            66,19,35,56,72,75,57,28,48,108,160,171,690,169,506,494,472,173,607,170,178,168,
            139,119,93,106,170,209,337,1385,567,192,142,199,162,148,176,350,248,202,89,44,18,21,
            117,89,69,53,19,20,82,74,88,54,18,35,71,58,312,203,727,160,245,567,321,75,
            132,161,371,187,177,168,166,122,115,100,162,164,1309,953,222,205,365,409,280,228,336,178,
            103,69,103,54,54,53,22,76,75,58,57,49,20,71,85,54,52,218,192,273,677,215,
            120,623,463,264,199,188,159,96,90,250,119,156,176,198,206,218,1321,674,231,303,480,259,
            627,202,350,204,62,56,54,53,84,62,31,57,57,56,81,56,43,58,57,57,50,63,
            212,429,152,104,778,152,150,87,174,189,154,136,132,125,117,119,92,79,77,190,902,627,
            232,245,221,216,242,183,317,205,152,74,72,34,19,59,83,80,57,31,18,67,77,82,
            42,18,19,95,341,213,662,111,106,563,618,225,304,147,167,235,261,316,190,112,72,160,
            115,211,984,959,226,284,248,224,187,198,329,200,81,56,55,54,68,73,91,68,23,27,
            90,57,64,92,58,22,54,76,218,185,653,163,98,69,105,462,170,761,116,175,139,147,
            188,180,165,164,161,174,988,675,183,203,199,192,268,246,238,286,160,95,67,76,88,86,
            77,83,31,21,21,22,80,122,61,53,53,68,248,150,451,407,545,215,138,100,141,95,
            256,189,163,112,92,38,48,84,113,181,965,1131,347,276,248,130,148,208,298,226,116,39,
            30,56,54,95,54,34,55,55,56,61,53,58,54,51,31,95,266,735,202,423,543,165,
            127,125,94,57,106,95,84,109,74,55,100,276,225,208,192,281,392,195,332,312,189,236,
            332,145,91,53,38,39,51,92,90,84,53,23,43,55,61,66,61,37,55,147,267,521,
            178,121,108,110,164,168,285,223,201,226,363,311,299,358,149,205,175,375,1105,322,220,160,
            141,156,192,199,202,365,183,75,36,92,76,96,85,58,61,129,98,96,94,45,37,76,
            85,99,235,202,300,400,208,213,464,563,317,273,201,149,304,132,187,137,131,167,371,164,
            364,1065,364,234,250,374,242,230,264,135,94,54,68,87,75,29,45,55,54,104,55,22,
            49,81,53,50,52,66,245,150,79,210,379,157,666,548,128,265,220,139,110,122,116,98,
            110,112,146,135,880,1378,221,235,216,238,239,145,361,197,156,84,64,78,96,93,92,124,
            87,44,25,48,87,119,78,58,60,124,279,625,115,83,1018,1382,950,1266,950,2022,1181,779,
            245,354,192,168,137,147,1123,915,270,861,889,258,195,185,206,194,287,155,169,92,87,56,
            76,42,20,58,90,67,83,45,17,25,93,80,71,180,116,486,202,153,116,69,63,74,
            129,265,103,183,126,126,85,122,68,35,30,240,1304,741,283,283,270,271,250,228,327,208,
            188,116,87,95,89,38,18,29,59,110,102,39,18,27,56,88,93,38,33,233,432,188,
            111,71,83,60,172,128,269,61,64,95,65,90,92,166,160,343,1087,868,671,211,306,398,
            638,289,312,695,583,167,107,19,22,57,55,93,104,44,18,47,54,83,76,41,29,61,
            280,562,181,521,303,272,503,530,188,118,186,131,133,133,105,309,206,183,156,172,1237,613,
            200,229,364,368,233,231,346,195,68,34,58,55,56,101,83,39,44,57,54,53,104,44,
            24,57,79,53,150,161,734,179,163,690,565,182,166,199,193,169,176,144,99,125,288,125,
            161,299,1173,606,187,239,261,190,163,199,341,203,120,66,24,17,46,98,89,78,28,18,
            44,86,85,76,18,18,71,239,207,451,103,94,176,111,91,237,229,237,563,273,567,116,
            149,164,278,164,280,156,165,140,142,149,196,164,160,104,125,144,166,162,126,36,21,49,
            56,92,105,56,19,32,57,86,91,52,19,38,70,321,277,468,282,595,329,123,223,74,
            151,179,88,58,77,79,56,30,141,123,787,1311,490,211,170,193,247,282,346,168,109,60,
    };

    protected  class RegressionJumpCallback{
        //TODO redevelop accordingly

        final String features[];

        public RegressionJumpCallback(String featureNames[]){
            this.features = featureNames;
        }

        public double value[];

        public int periods[][] =  new int[0][0];

        Callback<int[][]> cb = new Callback<int[][]>() {
            @Override
            public void on(int[][] result) {
                periods = result;
            }
        };

        public void on(AutoregressionBasedPeriodicityDetector result) {
            for (int i=0;i<features.length;i++){
                result.set(features[i], value[i]);
            }
            result.learn(cb);

            result.free();
        }
    };

    protected RegressionJumpCallback runThroughPartialLondonConsumptionData(AutoregressionBasedPeriodicityDetector lrNode, boolean swapResponse, double periods[], double sinPhaseShifts[], double amplitudes[]) {
        RegressionJumpCallback rjc = new RegressionJumpCallback(new String[]{AbstractLinearRegressionTest.FEATURE});
        for (int i = 0; i < consumptionLondon.length; i++) {
            //assertTrue(rjc.bootstrapMode);
            rjc.value = new double[]{consumptionLondon[i]};
            lrNode.jump(i, new Callback<Node>() {
                @Override
                public void on(Node result) {
                    rjc.on((AutoregressionBasedPeriodicityDetector) result);
                }
            });
        }
        //assertFalse(rjc.bootstrapMode);
        return rjc;
    }


    @Test
    public void testLondonconsumptionAutoregression() {
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                AutoregressionBasedPeriodicityDetector lrNode = (AutoregressionBasedPeriodicityDetector) graph.newTypedNode(0, 0, AutoregressionBasedPeriodicityDetector.NAME);
                lrNode.setProperty(AutoregressionBasedPeriodicityDetector.BUFFER_SIZE_KEY, Type.INT, consumptionLondon.length);
                lrNode.setProperty(AutoregressionBasedPeriodicityDetector.AUTOREGRESSION_LAG_KEY, Type.INT, 48*3);
                lrNode.set(AbstractMLNode.FROM, "f1");

                RegressionJumpCallback rjc = runThroughPartialLondonConsumptionData(lrNode, false, new double[]{5.0}, new double[]{0.0}, new double[]{10.0});
                lrNode.free();
                graph.disconnect(null);

                assertEquals(1, rjc.periods.length);
                assertEquals(4, rjc.periods[0].length);
                assertEquals(1, rjc.periods[0][0]);
                assertEquals(48, rjc.periods[0][1]);
                assertEquals(96, rjc.periods[0][2]);
                assertEquals(143, rjc.periods[0][3]);
            }
        });

    }

    @Test
    public void testWeightedLevensteinDistance(){
        for (int i=1;i<allDistances.length;i++){
            int dist = AutoregressionBasedPeriodicityDetector.periodsSequenceDistance(allPeriods[i-1],allPeriods[i], 4);
            assertEquals(i+". Found: "+dist+", expected "+allDistances[i-1], allDistances[i-1],dist);
        }
    }

    public static final int WINDOW_LENGTH = 336;

    @Test
    public void testEntireLondonConsumptionPeriods(){
        final Graph graph = new GraphBuilder().withPlugin(new MLXPlugin()).withScheduler(new NoopScheduler()).build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                AutoregressionBasedPeriodicityDetector lrNode = (AutoregressionBasedPeriodicityDetector) graph.newTypedNode(0, 0, AutoregressionBasedPeriodicityDetector.NAME);
                lrNode.setProperty(AutoregressionBasedPeriodicityDetector.BUFFER_SIZE_KEY, Type.INT, WINDOW_LENGTH);
                lrNode.setProperty(AutoregressionBasedPeriodicityDetector.AUTOREGRESSION_LAG_KEY, Type.INT, 48*3);
                lrNode.set(AbstractMLNode.FROM, "f1");

                RegressionJumpCallback rjc = new RegressionJumpCallback(new String[]{AbstractLinearRegressionTest.FEATURE});
                for (int i = 0; i < allConsumption.length; i++) {
                    rjc.value = new double[]{allConsumption[i]};
                    lrNode.jump(i, new Callback<Node>() {
                        @Override
                        public void on(Node result) {
                            rjc.on((AutoregressionBasedPeriodicityDetector) result);
                        }
                    });
                    if (i >= WINDOW_LENGTH-1){
                        assertEquals(1, rjc.periods.length);
                        assertEquals(allPeriods[i-WINDOW_LENGTH+1].length, rjc.periods[0].length);
                        for (int j=0;j<allPeriods[i-WINDOW_LENGTH+1].length;j++){
                            assertEquals(allPeriods[i-WINDOW_LENGTH+1][j], rjc.periods[0][j]);
                        }
                    }
                }
            }
        });
    }

}
