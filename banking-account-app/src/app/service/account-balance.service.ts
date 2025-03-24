import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { AccountBalanceResponse } from '../model/account-balance';

@Injectable({
  providedIn: 'root'
})
export class AccountBalanceService {

  constructor(private http: HttpClient) { }

  getAllAccountBalances() {
    return this.http.get<AccountBalanceResponse>('/api/accounts')
  }
}
