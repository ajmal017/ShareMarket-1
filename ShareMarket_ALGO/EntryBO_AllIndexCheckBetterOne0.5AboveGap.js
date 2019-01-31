
var isTesting = false;//vvvvvvvvvvvvvvvvvvvvvvvvvvvIMP, make it to false

var entryFromLastPrice=parseFloat(0.01);
var slPerc=parseFloat(-0.4);
var isExitAtSL= true;
var liveDataFull={};
var cash=0;
var blockMsg="BO BLOCKED COMPLETELY";
var blockedFileName="bo_blocked.json";
var totalAmountPlaced = parseFloat(0);

var indices = ["nifty_auto","nifty_bank","nifty_energy","NIFTY_FIN_SERVICE","nifty_fmcg",
				"nifty_it","nifty_media","nifty_metal","nifty_midcap_50","nifty_pharma","NIFTY_PSU_BANK",
				"nifty_realty"];
var indicesData=[];

var entryTradeObjects = [],  exitTradeObjects = [], orderIds=[], circuitData=[];
var eligibleSymbols = []; var zeroQtySymbols = [];
var maxSymbolsToPlaceOrder = 100;
var investMoreIfGapPercGreaterThan = 1;
var investMoreMultiplyIfGapPercGreaterThan = 1;

var SLWhenHitPercAndWentNegative = parseFloat(-0.00);
var hitPercAndWentNegative = parseFloat(0.5);

var now = new Date();
var millisTill10 = new Date(now.getFullYear(), now.getMonth(), now.getDate(), hour, minute, second, 0) - now;
if (millisTill10 < 0) {
    millisTill10 += 86400000; 
	console.log("it's already after 9:15");
}


function collectEligibleSymbol(company, direction, zerodhaId, openPrice,gapUpPerc, gapDownPerc){
	var gapPerc = (direction=='SELL')? gapUpPerc : gapDownPerc;
	var eligibleSym = {
		Id: company,
		Direction: direction,
		ZerodhaId: zerodhaId,
		Count: 1,
		IntradayOpen: openPrice,
		GapPerc: gapPerc
	}
	return eligibleSym;
}
function generateOrderObject(company, limitPrice, transaction_type, order_type, variety, count, intradayOpen,squareoff, stoploss)
{
	if(order_type=='MARKET'){
		limitPrice=0;
	}
	var order = {
		Id:"",
		TransactionType:"",
		OrderType:"",
		Quantity:"",
		LimitPrice:"",
		TriggerPrice:"",
		Variety:"",
		Count:"",
		IntradayOpen:"",
		SquareOff:"",
		StopLoss:""
	};
	//clearOrder();
	order.Id=company;
	order.TransactionType= transaction_type;
	order.OrderType=order_type;
	order.Quantity=qty;
	order.LimitPrice=limitPrice;// in placeOrder req, for MARKET : 0, LIMIT: limitPrice
	order.TriggerPrice = 0;
	order.Variety=variety;
	order.Count=count;
	order.IntradayOpen=intradayOpen;
	order.SquareOff=squareoff;
	order.StopLoss=stopLoss;
	return order;
}

function isSymbolAlreadyExist(name, dir){
	var index = parseInt(eligibleSymbols.findIndex(a=> a.Id== name && a.Direction== dir));
	if(index>=0){
		var c = eligibleSymbols[index].Count;
		eligibleSymbols[index].Count = ++c;
		return true;
	}
	return false;
}

function isIndexStrategy(strategyType, entryType,openPrice, yestOpen, yestHigh, yestLow, yestClose){
	if(isIndexCheck){
		if(strategyType == 'gapWithCloseTest'){
			if(entryType == 'SELL'){
				return (niftyData.Open-niftyData.PrevClose)*100/niftyData.Open > gapWithCloseTest1/indexGapDiv;
			}else if (entryType == 'BUY'){
				return (niftyData.PrevClose-niftyData.Open)*100/niftyData.Open > gapWithCloseTest1/indexGapDiv;
			}
		}
	}
	return false;
}

function checkAndAddIndexStrategySymbol(strategyType, entryType, name, zerodhaId,openPrice, yestOpen, yestHigh, yestLow, yestClose,gapUpPerc, gapDownPerc){
	for(var i=0; i<indicesData.length; i++){
        var data = indicesData[i];
        niftyData.Open = data.open, niftyData.PrevClose = data.close;
        if(isIndexStrategy(strategyType, entryType,openPrice, yestOpen, yestHigh, yestLow, yestClose)){
            //console.log("index="+strategyType+","+name+","+entryType);
            if(!isSymbolAlreadyExist(name, entryType) ){
                eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, entryType, zerodhaId, openPrice,gapUpPerc, gapDownPerc);
            }
        }
    }
}
//gapWithCloseTest
function gapWithCloseTest(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max,gapUpPerc, gapDownPerc){
	var strategyType = 'gapWithCloseTest';
	if((openPrice-yestClose)*100/openPrice > gapWithCloseTest1 && (openPrice-yestClose)*100/openPrice < gapCutoff && openPrice < max && openPrice>min){
		checkAndAddIndexStrategySymbol(strategyType, "SELL", name, zerodhaId,openPrice, yestOpen, yestHigh, yestLow, yestClose,gapUpPerc, gapDownPerc);
		if(isIncludeOnlyGapCheck && !isSymbolAlreadyExist(name, "SELL") ){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "SELL", zerodhaId, openPrice,gapUpPerc, gapDownPerc);
		}
	}
	else if((yestClose-openPrice)*100/openPrice > gapWithCloseTest1 && (yestClose-openPrice)*100/openPrice < gapCutoff && openPrice < max && openPrice>min){
		checkAndAddIndexStrategySymbol(strategyType, "BUY", name, zerodhaId,openPrice, yestOpen, yestHigh, yestLow, yestClose,gapUpPerc, gapDownPerc);
		if(isIncludeOnlyGapCheck && !isSymbolAlreadyExist(name, "BUY") ){
			eligibleSymbols[eligibleSymbols.length] = collectEligibleSymbol(name, "BUY", zerodhaId, openPrice,gapUpPerc, gapDownPerc);
		}
	}
}


function getIndicesData(){
    for(var j=0; j< indices.length; j++){
        var liveDataFull = getPriceFromUpstox(indexExchange, indices[j], 'full');
        if(liveDataFull){
            indicesData.push(liveDataFull);
        }
	}
}
function getEligibleSymbols(){
	
	var name, zerodhaId, yestOpen, yestHigh, yestLow, yestClose, fileNo=0;
	var whatPrice = 'O', whichCandle='1', duration = "", liveDataFull;
	var size = Math.floor(symbols.length/1);
	var start = fileNo*size; var end = start+size;
    //get all indices open price
    getIndicesData();
	for(var i=start; i< end; i++){
		name=symbols[i][0]; zerodhaId=symbols[i][1]; yestOpen=parseFloat(symbols[i][2]); yestHigh=parseFloat(symbols[i][3]);
		yestLow=parseFloat(symbols[i][4]);yestClose=parseFloat(symbols[i][5]);
		openPrice=parseFloat(symbols[i][6]);
		gapUpPerc=parseFloat(symbols[i][7]); gapDownPerc=parseFloat(symbols[i][8]);
		avgClosePrev2day=parseFloat(symbols[i][9]); avgClosePrev3day=parseFloat(symbols[i][10]);
		if(openPrice==0){
			continue;
		}
        //check with index check for all indices
        gapWithCloseTest1=parseFloat(0.8);
		isIndexCheck=true;isIncludeOnlyGapCheck = false;
		gapWithCloseTest(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max,gapUpPerc,gapDownPerc);
        //check without indices check
		gapWithCloseTest1=parseFloat(0.8);
		isIndexCheck = false;isIncludeOnlyGapCheck = true;
		gapWithCloseTest(name, zerodhaId, openPrice, yestOpen, yestHigh, yestLow, yestClose, min, max,gapUpPerc,gapDownPerc);		
	}
}

function executeDummyOrder(whichTab, eligibleCount){
	if(eligibleCount==0) eligibleCount=1;
	var xhr = new XMLHttpRequest();
	var url = "https://kite.zerodha.com/api/orders/regular";
	var OrderData = "exchange=NSE&tradingsymbol="+whichTab+"&transaction_type=BUY&order_type=LIMIT&quantity="+eligibleCount+"&price=1&product=MIS&validity=DAY&disclosed_quantity=0&trigger_price=0&squareoff=0&stoploss=0&trailing_stoploss=0&variety=regular";
	
	xhr.open("POST", url, false);
	xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
	xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhr.setRequestHeader("x-csrftoken", csrfToken);
	xhr.setRequestHeader("x-kite-version", "1.2.0");
	xhr.send(OrderData);
}
function populateTradeObjects(eligibleSymbolsCount){
	var name, zerodhaId, yestOpen, yestHigh, yestLow, yestClose;
	var whatPrice = 'ltp', whichCandle='1', duration = 3, total = parseInt(0);
	if(eligibleSymbols.length> maxSymbolsToPlaceOrder){
		eligibleSymbols.sort(function(a,b){ 
			return b.GapPerc  - a.GapPerc;
		});
		eligibleSymbols.splice(maxSymbolsToPlaceOrder, (eligibleSymbols.length-maxSymbolsToPlaceOrder));
	}
	if(eligibleSymbols.length > maxSymbolsToPlaceOrder){
		console.log("Something is wrong")
		return;
	}
	console.log(eligibleSymbols);

	eligibleSymbols.forEach(arr=> {
		if(arr.GapPerc>investMoreIfGapPercGreaterThan){
			arr.Count = arr.Count*investMoreMultiplyIfGapPercGreaterThan;
		}
	});
	
	eligibleSymbols.forEach(a=> total+=a.Count);
	var amntToInvestPerSymbol=0;
	if(!isMultipleTab){
//		eligibleSymbolsCount = eligibleSymbols.length;
		eligibleSymbolsCount = total;
	}
	amntToInvestPerSymbol = invest/total;
//	amntToInvestPerSymbol = invest/eligibleSymbolsCount;
	
//	var eligibleSymbolsCountTemp=eligibleSymbolsCount;
	var eligibleSymbolsCountTemp=total;
	var isPlacingOtherStretegy = false;
	console.log("Started placing order");
	console.log("------------------------------executing Place Entry Orders");
	for(var i=0; i< eligibleSymbols.length; i++){
		name = eligibleSymbols[i].Id; direction = eligibleSymbols[i].Direction; zerodhaId = eligibleSymbols[i].ZerodhaId, count = eligibleSymbols[i].Count, intradayOpen = eligibleSymbols[i].IntradayOpen;
		var liveDataFull = getPriceFromUpstox(eqExchange,name, 'full');
		circuitData[circuitData.length] = {
			"name": name,
			"lower_circuit": liveDataFull.lower_circuit,
			"upper_circuit": liveDataFull.upper_circuit
		}
		if(liveDataFull.ltp==undefined || liveDataFull.ltp==null){
//			eligibleSymbolsCount--;
			eligibleSymbolsCount = eligibleSymbolsCount-count;
			amntToInvestPerSymbol = invest/eligibleSymbolsCount;
			continue;
		}
		if(!isTesting)
		{
			if(parseFloat(liveDataFull.total_buy_qty)*parseFloat(liveDataFull.ltp) < 1000 
					|| parseFloat(liveDataFull.total_sell_qty)*parseFloat(liveDataFull.ltp) < 1000 ){
//				eligibleSymbolsCount--;
				zeroQtySymbols[zeroQtySymbols.length] = {
					"index": i,
					"isPlacedOrder": false,
					"amountToInvest": amntToInvestPerSymbol
				};
				amntToInvestPerSymbol = invest/eligibleSymbolsCount;
				continue;
			}
		}
		price = liveDataFull.ltp;
		if(direction=='SELL')
		{
			entryPrice = price - (price*entryPercFromOpen)/100;
			entryPrice = getTickPrice(entryPrice);
			squareOff = getTickPrice(parseFloat(entryPrice*squareoffPerc/100));
			stopLoss = getTickPrice(parseFloat(entryPrice*stopLossPerc/100));
			entryTradeObjects[entryTradeObjects.length] = generateOrderObject(name, entryPrice, "SELL", typeOfOrder, boVarietyType, count, intradayOpen,squareOff, stopLoss);
			var isRejected = placeZerodhaOrder(entryTradeObjects[entryTradeObjects.length-1], amntToInvestPerSymbol, isPlacingOtherStretegy);
			if(isRejected==blockMsg){
				console.log("Exiting populateTradeObjects");
				return;
			}
			if(isRejected==true){
//				eligibleSymbolsCount--;
//				eligibleSymbolsCount = eligibleSymbolsCount-count;
//				amntToInvestPerSymbol = invest/eligibleSymbolsCount;
			}
		}
		else if(direction =='BUY'){
			entryPrice = price + (price*entryPercFromOpen)/100;
			entryPrice = getTickPrice(entryPrice);
			squareOff = getTickPrice(parseFloat(entryPrice*squareoffPerc/100));
			stopLoss = getTickPrice(parseFloat(entryPrice*stopLossPerc/100));
			entryTradeObjects[entryTradeObjects.length] = generateOrderObject(name, entryPrice, "BUY", typeOfOrder, boVarietyType, count,intradayOpen,squareOff, stopLoss);
			var isRejected = placeZerodhaOrder(entryTradeObjects[entryTradeObjects.length-1], amntToInvestPerSymbol, isPlacingOtherStretegy);
			if(isRejected==blockMsg){
				console.log("Exiting populateTradeObjects");
				return;
			}
			if(isRejected==true){
//				eligibleSymbolsCount--;
//				eligibleSymbolsCount = eligibleSymbolsCount-count;
//				amntToInvestPerSymbol = invest/eligibleSymbolsCount;
			}
		}
	}
	amntToInvestPerSymbol = invest/eligibleSymbolsCountTemp;
	
	for(var k=0;k<5; k++){
		if(zeroQtySymbols.filter(a => a.isPlacedOrder==false).length > 0)
		{
			rerunZeroQtySymbols();
			sleep(10000);
		}
	}
	if(isExitAtSL){
		exitOrders();
	}
}

function placeOtherStrategy(){
	var isRejected=false, amntToInvestPerSymbol=0;
	var isPlacingOtherStretegy = true;
	console.log("------------------------------executing placeOtherStrategy");
	for (var i=0; i< otherStrategyObjects.length; i++){
		if(otherStrategyObjects[i].TransactionType=='SELL'){
			var intradayOpen = otherStrategyObjects[i].LimitPrice;
			var trig = getTickPrice(parseFloat(intradayOpen + (intradayOpen*availableAtCheaperPricePercFromOpen/100)));
			otherStrategyObjects[i].LimitPrice = trig;
			amntToInvestPerSymbol = otherStrategyObjects[i].Quantity * otherStrategyObjects[i].LimitPrice;
			isRejected = placeZerodhaOrder(otherStrategyObjects[i], amntToInvestPerSymbol, isPlacingOtherStretegy);
			if(isRejected==blockMsg){
				console.log("Exiting placeOtherStrategy");
				return;
			}
		}
		else if(otherStrategyObjects[i].TransactionType=='BUY'){
			var intradayOpen = otherStrategyObjects[i].LimitPrice;
			var trig = getTickPrice(parseFloat(intradayOpen - (intradayOpen*availableAtCheaperPricePercFromOpen/100)));
			otherStrategyObjects[i].LimitPrice = trig;
			amntToInvestPerSymbol = otherStrategyObjects[i].Quantity * otherStrategyObjects[i].LimitPrice;
			isRejected = placeZerodhaOrder(otherStrategyObjects[i], amntToInvestPerSymbol, isPlacingOtherStretegy);
			if(isRejected==blockMsg){
				console.log("Exiting placeOtherStrategy");
				return;
			}
		}
	}
}

function rerunZeroQtySymbols(){
	var isPlacingOtherStretegy = false;
	var name, zerodhaId, yestOpen, yestHigh, yestLow, yestClose;
	var whatPrice = 'ltp', whichCandle='1', duration = 3, total = parseInt(0);
	var amntToInvestPerSymbol=0,index=0, placeOrder;
	console.log("------------------------------executing rerunZeroQtySymbols");
	for(var i=0; i< zeroQtySymbols.length; i++){
		index = zeroQtySymbols[i].index;
		placeOrder = zeroQtySymbols[i].isPlacedOrder;
		amntToInvestPerSymbol = zeroQtySymbols[i].amountToInvest;
		if(zeroQtySymbols[i].isPlacedOrder == true){
			continue;
		}
		name = eligibleSymbols[index].Id; direction = eligibleSymbols[index].Direction; zerodhaId = eligibleSymbols[index].ZerodhaId, count = eligibleSymbols[index].Count, intradayOpen = eligibleSymbols[index].IntradayOpen;
		var liveDataFull = getPriceFromUpstox(eqExchange,name, 'full');
		if(parseFloat(liveDataFull.total_buy_qty)*parseFloat(liveDataFull.ltp) < 1000 
				|| parseFloat(liveDataFull.total_sell_qty)*parseFloat(liveDataFull.ltp) < 1000 ){
			
			continue;
		}
		price = liveDataFull.ltp;
		if(direction=='SELL')
		{
			zeroQtySymbols[i].isPlacedOrder=true;
			entryPrice = price - (price*entryPercFromOpen)/100;
			entryPrice = getTickPrice(entryPrice);
			squareOff = getTickPrice(parseFloat(entryPrice*squareoffPerc/100));
			stopLoss = getTickPrice(parseFloat(entryPrice*stopLossPerc/100));
			entryTradeObjects[entryTradeObjects.length] = generateOrderObject(name, entryPrice, "SELL", typeOfOrder, boVarietyType, count,intradayOpen,squareOff, stopLoss);
			var isRejected = placeZerodhaOrder(entryTradeObjects[entryTradeObjects.length-1], amntToInvestPerSymbol, isPlacingOtherStretegy);
			if(isRejected==blockMsg){
				console.log("Exiting rerunZeroQtySymbols");
				return;
			}
		}
		else if(direction =='BUY'){
			zeroQtySymbols[i].isPlacedOrder=true;
			entryPrice = price + (price*entryPercFromOpen)/100;
			entryPrice = getTickPrice(entryPrice);
			squareOff = getTickPrice(parseFloat(entryPrice*squareoffPerc/100));
			stopLoss = getTickPrice(parseFloat(entryPrice*stopLossPerc/100));
			entryTradeObjects[entryTradeObjects.length] = generateOrderObject(name, entryPrice, "BUY", typeOfOrder, boVarietyType, count, intradayOpen,squareOff, stopLoss);
			var isRejected = placeZerodhaOrder(entryTradeObjects[entryTradeObjects.length-1], amntToInvestPerSymbol, isPlacingOtherStretegy);
			if(isRejected==blockMsg){
				console.log("Exiting rerunZeroQtySymbols");
				return;
			}
		}
	}
}
function placeZerodhaOrder(entryTradeObjects, amntToInvestPerSymbol, isPlacingOtherStretegy){
	var isRejected=false;
	console.log(entryTradeObjects);
	amntToInvestPerSymbol = amntToInvestPerSymbol * entryTradeObjects.Count;
	console.log(amntToInvestPerSymbol);
	
	if(isPlaceOrder){
		totalAmountPlaced = totalAmountPlaced + parseInt(amntToInvestPerSymbol);
		console.log("Placing orders as Invest="+invest+", placed="+totalAmountPlaced+"");
		if(totalAmountPlaced > invest){
			console.log("Rejecting placing orders as Invest="+invest+", placed="+totalAmountPlaced+"======================");
			return false;
		}
		var orderId = PlaceOrder(entryTradeObjects, amntToInvestPerSymbol);
		
		if(orderId!=0){
			isRejected = isOrderRejected(orderId);
			if(isRejected == blockMsg){
				console.log("Exiting placeZerodhaOrder");
				return blockMsg;
			}
		}
		//!isRejected && !isPlacingOtherStretegy
		if(!isRejected && !isPlacingOtherStretegy)
		{
			var noOfShares = amntToInvestPerSymbol/entryTradeObjects.LimitPrice;
			if(noOfShares<=1){
				noOfShares=1;
			}
			entryTradeObjects.Quantity = parseInt(noOfShares);
			otherStrategyObjects[otherStrategyObjects.length] = entryTradeObjects;
		}
	}
	return isRejected;
}

function getPositions(){
	try{
		var url="https://kite.zerodha.com/api/portfolio/positions";
		var xhr = new XMLHttpRequest();
		xhr.open("GET", url, false);
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.send();
		if(!isJsonString(xhr.response)){
			return 0;
		}
		return JSON.parse(xhr.response);
	}catch(err){
		console.log(err);
		return 0;
	}
}

function placeStopLossOrders()
{
	console.log("started placing stoploss orders");
	var res = getPositions();
	for(var i=0; i<res.data.net.length; i++){
		var obj = res.data.net[i];
		var dir="", quantity=0, last_price=0, cIndex=0, upperCircuit=parseFloat(0), lowerCircuit=parseFloat(0);
		if(parseInt(obj.quantity)!=0)
		{
			cIndex = circuitData.findIndex(object => object.name==obj.tradingsymbol);
			if(cIndex>=0)
			{
				upperCircuit = circuitData[cIndex].upper_circuit;
				lowerCircuit = circuitData[cIndex].lower_circuit;
				if(parseInt(obj.buy_quantity) > parseInt(obj.sell_quantity)){
					dir="SELL";
					quantity = parseInt(obj.buy_quantity)-parseInt(obj.sell_quantity);
					last_price = lowerCircuit+(lowerCircuit*stopLossPercFromCircuit)/100;
					last_price = getTickPrice(last_price);
				}else if(parseInt(obj.sell_quantity) > parseInt(obj.buy_quantity)){
					dir="BUY";
					quantity = parseInt(obj.sell_quantity)-parseInt(obj.buy_quantity);
					last_price = upperCircuit-(upperCircuit*stopLossPercFromCircuit)/100;
					last_price = getTickPrice(last_price);
				}
				obj.tradingsymbol = obj.tradingsymbol.replace("&", "%26");
				var exitOrder = generateOrderObject(obj.tradingsymbol, last_price, dir, typeOfOrder, varietyType, quantity,0,0,0);
				exitOrder.Quantity=quantity;
				PlaceStopLossOrder(exitOrder, varietyType);
			}
		}
	}
}

function PlaceStopLossOrder(Order, variety)
{
	try{
		Order.TriggerPrice=Order.LimitPrice;
		var xhr = new XMLHttpRequest();
		var url = "https://kite.zerodha.com/api/orders/"+varietyType;
		var OrderData = "exchange=NSE&tradingsymbol="+Order.Id+"&transaction_type="+Order.TransactionType+"&order_type=SL-M&quantity="+Order.Quantity+"&price=0&product=MIS&validity=DAY&disclosed_quantity=0&trigger_price="+Order.TriggerPrice+"&squareoff=0&stoploss=0&trailing_stoploss=0&variety="+variety+"&user_id=DP3137";
		
		xhr.open("POST", url, false);
		xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
		xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send(OrderData);
	}catch(err){
		console.log(err);
	}
}

function isOrderRejected(orderId){
	var msg = "RMS:Blocked for  EQ  nse_cm  BO Remarks: Due to expected volatility, BO and CO will remain blocked.  block type: ALL";
	try{
		var xhr = new XMLHttpRequest();xhr.open("GET", "https://kite.zerodha.com/api/orders", false);
		xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
		xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send();
		var res = JSON.parse(xhr.response);
		var index = res.data.findIndex(obj=>obj.order_id == orderId);
		if(res.data[index] == undefined){
			return false;
		}
		
		if(res.data[index].status == 'REJECTED')
		{
			var statusMsg = res.data[index].status_message;
			if(statusMsg && statusMsg.includes(msg) || isTesting)
			{
				downloadToFile("BO BLOCKED COMPLETELY" , blockedFileName);
				return blockMsg;
			}
			return true;
		}else{
			return false;
		}
	}catch(err){
		console.log(err);
		return false;
	}
}

function downloadToFile(data, fileName){
	var blob=new Blob([data]); var link=document.createElement('a');
	link.href=window.URL.createObjectURL(blob); 
	link.download=fileName;
	link.click();
}

function getEligibleSymbolCountAcrossTabs(){
	try{
		var xhr = new XMLHttpRequest();xhr.open("GET", "https://kite.zerodha.com/api/orders", false);
		xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
		xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send();
		var res = JSON.parse(xhr.response);
		var totalCount = 0;
		if(res.data.findIndex(obj=>obj.tradingsymbol == 'tab_1')<0){
			getEligibleSymbolCountAcrossTabs();
		}
		if(res.data.findIndex(obj=>obj.tradingsymbol == 'tab_2')<0){
			getEligibleSymbolCountAcrossTabs();
		}
		if(res.data.findIndex(obj=>obj.tradingsymbol == 'tab_3')<0){
			getEligibleSymbolCountAcrossTabs();
		}
		if(res.data.findIndex(obj=>obj.tradingsymbol == 'tab_4')<0){
			getEligibleSymbolCountAcrossTabs();
		}
		for(var i=0; i< res.data.length; i++){
			if(res.data[i].tradingsymbol == 'tab_1'){
				totalCount += res.data[i].quantity;
			}else if(res.data[i].tradingsymbol == 'tab_2'){
				totalCount += res.data[i].quantity;
			}else if(res.data[i].tradingsymbol == 'tab_3'){
				totalCount += res.data[i].quantity;
			}else if(res.data[i].tradingsymbol == 'tab_4'){
				totalCount += res.data[i].quantity;
			}
		}
		return totalCount;
	}catch(err){
		console.log(err);
	}
}

function startEntry(){
	console.log(new Date());
	getEligibleSymbols();
	console.log("Eligible symbols collected");
	
	var eligibleSymbolsCount=0;
	if(isMultipleTab){
		executeDummyOrder(whichTab, eligibleSymbols.length);
		eligibleSymbolsCount = getEligibleSymbolCountAcrossTabs();
	}
	 
	if(eligibleSymbols.length>0){
		console.log("Across tabs eligible symbols count="+eligibleSymbolsCount);
		var isRejected = populateTradeObjects(eligibleSymbolsCount);
		if(isRejected==blockMsg){
			console.log("Aborting======================");
			return;
		}
		console.log("This tab eligible symbols count="+eligibleSymbols.length);
	}
	console.log(new Date());
}

function executedOrders(orderId, symbol){
	var executedOrders = {
		OrderId:"",
		Symbol:"",
		Status: ""
	};
	executedOrders.OrderId=orderId;
	executedOrders.Symbol=symbol;
	executedOrders.Status="";
	return executedOrders;
}

function PlaceOrder(Order, amntToInvestPerSymbol)
{ 
	try{
		var xhr = new XMLHttpRequest();
		var url = "https://kite.zerodha.com/api/orders/"+Order.Variety;
		noOfShares = amntToInvestPerSymbol/Order.LimitPrice;
		if(noOfShares<=1){
			noOfShares=1;
		}
		Order.Quantity = parseInt(noOfShares);
		var OrderData = "exchange=NSE&tradingsymbol="+Order.Id+"&transaction_type="+Order.TransactionType+"&order_type="+Order.OrderType+"&quantity="+Order.Quantity+"&price="+Order.LimitPrice+"&product=MIS&validity=DAY&disclosed_quantity=0&trigger_price="+Order.TriggerPrice+"&squareoff="+Order.SquareOff+"&stoploss="+Order.StopLoss+"&trailing_stoploss=0&variety="+Order.Variety+"";
		
		xhr.open("POST", url, false);
		xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
		xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send(OrderData);
		
		if(!isJsonString(xhr.response)){
			return 0;
		}
		var res = JSON.parse(xhr.response);
		if(res != undefined && res.data == undefined){
			return 0;
		}
		return res.data.order_id;
	}catch(err){
		console.log(err);
		return 0;
	}
}

function isJsonString(str) {
    try {
        JSON.parse(str);
    } catch (e) {
        return false;
    }
    return true;
}

function getPriceFromUpstox(exchange, name, type){
	liveDataFull={};
	try{
		var xhr = new XMLHttpRequest();
		xhr.open("GET", 'https://api.upstox.com/live/feed/now/'+exchange+'/'+name+'/'+type+'', false);
		xhr.setRequestHeader("authorization", "Bearer "+upstoxAccessToken);
		xhr.setRequestHeader("x-api-key", upstoxApiKey);
		xhr.send();
		var res = JSON.parse(xhr.response);
		
		if(res.data != null && res.data != undefined){
			liveDataFull.ltp=res.data.ltp;
			liveDataFull.total_buy_qty=res.data.total_buy_qty;
			liveDataFull.total_sell_qty=res.data.total_sell_qty;
			liveDataFull.name=name;
			liveDataFull.open=res.data.open;
			liveDataFull.close=res.data.close;
			liveDataFull.upper_circuit=res.data.upper_circuit;
			liveDataFull.lower_circuit=res.data.lower_circuit;
			if(type=='full'){
				globalResponse=globalResponse+name+"\n";
				if(res.data.asks!=null && res.data.asks != undefined && res.data.asks.length>=2){	globalResponse=globalResponse+JSON.stringify(res.data.asks[0].quantity*res.data.asks[0].price+res.data.asks[1].quantity*res.data.asks[1].price)+"\n";
				}	
				if(res.data.bids!=null && res.data.bids != undefined && res.data.bids.length>=2){		globalResponse=globalResponse+JSON.stringify(res.data.bids[0].quantity*res.data.bids[0].price+res.data.bids[1].quantity*res.data.bids[1].price)+"\n\n";
				}		
			}
		}
	}
	catch(err){
		console.log(err);
		sleep(5000);
		getPriceFromUpstox(exchange, name, type);
	}finally{
		return liveDataFull;
	}
}

function getPriceFromZerodha(token, duration, whatPrice, whichCandle)
{
	var result="";
	var from=getDate(0), to =getDate(0);
	var getUrl = "https://kitecharts.zerodha.com/api/chart/"+token+"/"+duration+"minute?public_token="+csrfToken+"&user_id=DP3137&api_key=kitefront&access_token="+accessToken+"&from="+from+"&to="+to+"&ciqrandom=1523897795455";
	
	$.ajax({
		statusCode: {
			500: function() {
				console.error("Error while placing order.");
			}
		},
		url : getUrl,
		type: "GET",
		async: false,
		success: function(data, textStatus, jqXHR)
		{		
			if(data != null && data.status == "success"){
				result = getO_H_L_C(data, whatPrice, whichCandle);
			}
		},
		error: function (jqXHR, textStatus, errorThrown)
		{
			console.log("error");
		}
	});
	return result;
}

//get margin from zerodha
function getCashInAccount()
{
	cash=0;
	try{
		var xhr = new XMLHttpRequest();
		xhr.open("GET", 'https://kite.zerodha.com/api/user/margins', false);
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send();
		var res = JSON.parse(xhr.response);
		cash = res.data.equity.available.cash;
	}catch(err){
		console.log(err);
		sleep(1000);
		getCashInAccount();
	}finally{
		return cash;
	}
}

//Utility functions------------------------------------------------------------------------------
function sleep(milliseconds) {
  var start = new Date().getTime();
  for (var i = 0; i < 1e30; i++) {
    if ((new Date().getTime() - start) > milliseconds){
      break;
    }
  }
}

function getDate(dayToSubstract) {
    var date = new Date();
    date.setDate(date.getDate()-dayToSubstract);
    return date.getFullYear() + '-' + ('0' + (date.getMonth()+1)).slice(-2) + '-' + ('0' + date.getDate()).slice(-2);
}

function getTickPrice(price){
	var tickPrice = 0;
	toAdd = 10-(((parseFloat(price).toFixed(2))*100)%10);
	if(parseInt(toAdd) < 5){
		tickPrice = (((parseFloat(price).toFixed(2))*100)+toAdd)/100;
	}
	if(parseInt(toAdd) >= 5){
		tickPrice = (((parseFloat(price).toFixed(2))*100)+(toAdd-5))/100;
	}
	return tickPrice;
}
function getO_H_L_C(data, whatPrice, whichCandle)
{
	var index;
	if(whatPrice=='O') index = 1
	else if(whatPrice=='H') index = 2
	else if(whatPrice=='L') index = 3
	else if(whatPrice=='C') index = 4;
	var price=-999;
	if(data.data.candles.length!=0 && data.data.candles.length>=whichCandle){
		price = data.data.candles[whichCandle][index];
	}
	return price;
}

function isNonNull(value){
	if(value != undefined && value != "" && value != null) return true; else return false;
}


function getPreOpen(){
	var xhr = new XMLHttpRequest();xhr.open("GET", 'https://www.nseindia.com/live_market/dynaContent/live_analysis/pre_open/all.json', false);
	xhr.send();
	var blob=new Blob([xhr.response]);
	var link=document.createElement('a');
	link.href=window.URL.createObjectURL(blob);
	link.download="all.json";link.click();
}

function exitOrders()
{
	console.log("Checking SL to exit orders");
	var isExitAtSL=false;
	while(!isExitAtSL){
		var totalProfit=0,totalInvestment = parseFloat(0);
		var res = getPositions();
		if(res == 0){
			sleep(10000);
			continue;
		}
		if(res != null && res.data != null && res.data.net != null){
			res.data.net.forEach(obj=>totalInvestment+= Math.abs(obj.average_price*obj.quantity));
			res.data.net.forEach(obj=>totalProfit+=obj.unrealised);
		}else{
			sleep(20000);
			continue;
		}
		var maxSL=totalInvestment*slPerc/100;
		var targetProfitToReduceSL = (totalInvestment*hitPercAndWentNegative)/100;
		console.log("Total profit="+totalProfit+" and SL="+slPerc+", loss="+maxSL);
		
		if(parseFloat(totalProfit) > parseFloat(targetProfitToReduceSL)){
			slPerc = SLWhenHitPercAndWentNegative;
		}
		if(totalProfit < maxSL){
			var isExitedAll = false;
			while(!isExitedAll){
				isExitedAll = isExitCompleted();
				sleep(10000);
			}
			isExitAtSL=true;
			console.log("Exited");
		}
		sleep(20000);
	}
	cancelAllOpenOrdersAfterExit();
}

function cancelAllOpenOrdersAfterExit(){
	var res = getAllOpenOrders();
	
	if(res && res.data){
		var openOrders = res.data.filter(obj=> obj.status == "OPEN" && obj.variety=='bo');
		openOrders.forEach(o => {
			cancelOrder(o.order_id);
		})
	}
}

function isExitCompleted(){
	console.log("started exit");
	var res = getPositions();
	var boOrder="";
	if(res == 0){
		sleep(10000);
		return false;
	}
	for(var i=0; i<res.data.net.length; i++){
		var obj = res.data.net[i];
		if(obj.product == 'BO')
		{
			var dir="", quantity=0, last_price=0;
			if(parseInt(obj.quantity)!=0)
			{
				if(parseInt(obj.buy_quantity) > parseInt(obj.sell_quantity)){
					dir="SELL";
					var liveDataLtp = getPriceFromUpstox(eqExchange,obj.tradingsymbol, 'ltp');
					last_price = liveDataLtp.ltp? liveDataLtp.ltp: obj.last_price;
					last_price = last_price-(last_price*entryFromLastPrice)/100;
					last_price = getTickPrice(last_price);
					boOrder = getBOOpenOrder(obj.tradingsymbol, dir);
				}else if(parseInt(obj.sell_quantity) > parseInt(obj.buy_quantity)){
					dir="BUY";
					var liveDataLtp = getPriceFromUpstox(eqExchange,obj.tradingsymbol, 'ltp');
					last_price = liveDataLtp.ltp? liveDataLtp.ltp: obj.last_price;
					last_price = last_price+(last_price*entryFromLastPrice)/100;
					last_price = getTickPrice(last_price);
					boOrder = getBOOpenOrder(obj.tradingsymbol, dir);
				}
				obj.tradingsymbol = obj.tradingsymbol.replace("&", "%26");
				if(boOrder){
					for(var k=0; k< boOrder.length; k++){
						PlaceExitOrder(boOrder[k], last_price);
					}
				}
			}
		}	
	}
	if(res.data.net.filter(a => a.quantity!=0 && a.product == 'BO').length >0){
		return false;
	}else{
		return true;
	}
}

function PlaceExitOrder(Order, last_price)
{
	try{
		var xhr = new XMLHttpRequest();
		var url = "https://kite.zerodha.com/api/orders/bo/"+Order.order_id;
		var OrderData = "exchange=NSE&tradingsymbol="+Order.tradingsymbol+"&transaction_type="+Order.transaction_type+"&order_type=LIMIT&quantity="+Order.quantity+"&price="+last_price+"&product=MIS&validity=DAY&disclosed_quantity=0&trigger_price=0&squareoff=0&stoploss=0&trailing_stoploss=0&variety=bo&user_id=DP3137&order_id="+Order.order_id+"&parent_order_id=";
		
		xhr.open("PUT", url, false);
		xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
		xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send(OrderData);
	}catch(err){
		console.log(err);
	}
}

function cancelOrder(orderId){
	try{
		var xhr = new XMLHttpRequest();
		var url = "https://kite.zerodha.com/api/orders/bo/"+orderId+"?order_id="+orderId+"&variety=bo";
		
		xhr.open("DELETE", url, false);
		xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
		xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send();
	}catch(err){
		console.log(err);
	}
}

function getBOOpenOrder(symbol, dir)
{
	try{
		var xhr = new XMLHttpRequest();xhr.open("GET", "https://kite.zerodha.com/api/orders", false);
		xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
		xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send();
		var res = JSON.parse(xhr.response);
		if(res.data){
			return res.data.filter(obj=> obj.tradingsymbol == symbol && obj.status == "OPEN" && obj.transaction_type == dir && obj.variety=='bo');
		}
		return null;
	}catch(err){
		console.log(err);
		return null;
	}
}

function getAllOpenOrders()
{
	try{
		var xhr = new XMLHttpRequest();xhr.open("GET", "https://kite.zerodha.com/api/orders", false);
		xhr.setRequestHeader("Accept", "application/json, text/plain, */*");
		xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		xhr.setRequestHeader("x-csrftoken", csrfToken);
		xhr.setRequestHeader("x-kite-version", "1.2.0");
		xhr.send();
		var res = JSON.parse(xhr.response);
		return res;
	}catch(err){
		console.log(err);
	}
}

function start(){
	invest = parseFloat(getCashInAccount())*marginMultipler;
	if(isTesting){
		maxSymbolsToPlaceOrder = 10;
		startEntry();
	}else{
		scheduleZerodhaEntryOrders();
	}
}

function scheduleZerodhaEntryOrders(){
	now = new Date();
	millisTill10 = new Date(now.getFullYear(), now.getMonth(), now.getDate(), hour, parseInt(minute), second, 0) - now;
	if (millisTill10 < 0) {
		millisTill10 += 86400000; 
		console.log("it's already after time");
	}
	setTimeout(startEntry, millisTill10);
}

start();
