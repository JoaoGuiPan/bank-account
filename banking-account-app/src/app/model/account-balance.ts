export interface AccountBalance {
    account: string;
    userLastName: string;
    balance: number;
}

export interface AccountBalanceResponse {
    accounts: AccountBalance[];
}
